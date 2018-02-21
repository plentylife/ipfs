package life.plenty.model.permissions

import life.plenty.model
import life.plenty.model.actions.ActionOnGraphTransform
import life.plenty.model.connection.DataHub
import life.plenty.model.octopi.{Transaction, User, Vote}
import life.plenty.model.octopi.definition.Hub
import life.plenty.model.octopi.pseudo.Wallet
import rx.Ctx

import scala.util.Try

class NotEnoughThanks extends Exception
class NotEnoughVotingPower extends Exception

class FundsCheck(override val withinOctopus: User) extends ActionOnGraphTransform {
  private implicit val ctx = withinOctopus.ctx

  override def onConnectionAdd(connection: DataHub[_]): Either[Exception, Unit] = {
    connection.value match {
      case t: Transaction ⇒
        Try {
          t.getOnContribution.now match {
            case Some(c) ⇒ val w = new Wallet(withinOctopus, c)
              if (w.getThanksBalance.now + w.getUsableThanksLimit.now - t.getAmount.now.get >= 0) {
                Right()
              } else {
                model.console.error("Not enough funds!")
                Left(new NotEnoughThanks)
              }
            case None ⇒ Left(new Exception("Transaction did not have a parent"))
          }
        }.recover {
          case e: Throwable ⇒ Left(new Exception("Operational exception while trying to check funds"))
        }.get

      case v: Vote ⇒
        Try {
          v.parentAnswer.now match {
            case Some(c) ⇒ val w = new Wallet(withinOctopus, c)
              if (w.getUsableVotes.now - v.getAmount.now.get >= 0) {
                Right()
              } else {
                model.console.error("Not enough voting power!")
                Left(new NotEnoughVotingPower)
              }
            case None ⇒ Left(new Exception("Vote did not have a parent"))
          }
        }.recover {
          case e: Throwable ⇒ Left(new Exception("Operational exception while trying to check funds (votes)"))
        }.get

      case _ ⇒ Right()
    }
  }

  override def onConnectionRemove(connection: DataHub[_]): Either[Exception, Unit] = ???
}
