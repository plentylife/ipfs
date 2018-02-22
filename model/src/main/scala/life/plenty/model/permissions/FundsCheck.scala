package life.plenty.model.permissions

import life.plenty.model
import life.plenty.model.actions.{ActionOnFinishDataLoad, ActionOnGraphTransform}
import life.plenty.model.connection.DataHub
import life.plenty.model.octopi.{Transaction, User, Vote}
import life.plenty.model.octopi.definition.Hub
import life.plenty.model.octopi.pseudo.Wallet

import scala.concurrent.ExecutionContext.Implicits.global
import rx.Ctx

import scala.concurrent.{Future, Promise}
import scala.util.Try

class NotEnoughThanks extends Exception
class NotEnoughVotingPower extends Exception

class FundsCheck(override val withinOctopus: User) extends ActionOnGraphTransform {
  private implicit val ctx = withinOctopus.ctx

  private lazy val dataLoader = withinOctopus.getTopModule({case m: ActionOnFinishDataLoad ⇒ m})

  override def onConnectionAdd(connection: DataHub[_]): Future[Unit] = {
    val promise = Promise[Unit]()
    connection.value match {
      case t: Transaction ⇒
        model.console.trace(s"Funds check on transaction $connection")
        Try {
          dataLoader.get.onFinishLoad(() ⇒ {
            t.getOnContribution.now match {
              case Some(c) ⇒ val w = new Wallet(withinOctopus, c)
                if (w.getThanksBalance.now + w.getUsableThanksLimit.now - t.getAmount.now.get >= 0) {
                  promise.success()
                } else {
                  model.console.error("Not enough funds!")
                  promise.failure(new NotEnoughThanks)
                }
              case None ⇒ Left(new Exception("Transaction did not have a parent"))
            }
          })
        }.recover {
          case e: Throwable ⇒ promise.failure(new Exception("Operational exception while trying to check funds"))
        }.get

      case v: Vote ⇒
        model.console.trace(s"Funds check on vote $connection")
        Try {
          dataLoader.get.onFinishLoad(() ⇒ {
            v.parentAnswer.now match {
              case Some(c) ⇒ val w = new Wallet(withinOctopus, c)
                if (w.getUsableVotes.now - v.getAmount.now.get >= 0) {
                  promise.success()
                } else {
                  model.console.error("Not enough voting power!")
                  promise.failure(new NotEnoughVotingPower)
                }
              case None ⇒ Left(new Exception("Vote did not have a parent"))
            }
          })
        }.recover {
          case e: Throwable ⇒
            promise.failure(new Exception("Operational exception while trying to check funds (votes)"))
        }.get

      case _ ⇒
        model.console.trace(s"Funds check on $connection. Auto pass.")
        promise.success()
    }
    promise.future
  }
}
