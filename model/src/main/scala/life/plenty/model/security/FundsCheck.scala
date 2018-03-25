package life.plenty.model.security

import life.plenty.model
import life.plenty.model.actions.{ActionOnFinishDataLoad, ActionOnGraphTransform}
import life.plenty.model.connection.DataHub
import life.plenty.model.hub.{Transaction, User, Vote}
import life.plenty.model.hub.definition.Hub
import life.plenty.model.hub.pseudo.Wallet

import scala.concurrent.ExecutionContext.Implicits.global
import rx.Ctx

import scala.concurrent.{Future, Promise}
import scala.util.Try

import rx.async._
import rx.async.Platform._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

trait FundsError extends Exception {
  val user: User
}
class NotEnoughThanks(override val user: User) extends FundsError
class NotEnoughVotingPower(override val user: User) extends FundsError

// fixme. this relies on loading order of the DB. if the transactions are out of order, this will trigger

class FundsCheck(override val hub: User) extends ActionOnGraphTransform {
  private implicit val ctx = hub.ctx

  // fixme put wallet here

  override def onConnectionAdd(connection: DataHub[_]): Future[Unit] = {
    val promise = Promise[Unit]()

    connection.value match {
      case t: Transaction ⇒
        model.console.trace(s"Funds check on transaction $t ${t.id}")
        Try {
          // the dataloaders must be present
            t.getTopModule({case m: ActionOnFinishDataLoad ⇒ m}).get.onFinishLoad(() ⇒ {
//              model.console.trace("Funds check finish load")
              t.getFrom.now match {
                case Some(from) ⇒
                  // if the user is not the sender, skip
                  if (hub.id == from.id) {

                    val w = new Wallet(hub, null)
                    if (w.getThanksBalance.now + w.getUsableThanksLimit.now - t.getAmount.now.get >= 0) {
                      model.console.trace(s"Funds check passed transaction $t ${t.id}")
                      promise.success()
                    } else {
                      model.console.error("Funds check failed transaction. Not enough funds!")
                      promise.failure(new NotEnoughThanks(hub))
                    }

                  } else {
                    model.console.trace(s"Funds check auto passed transaction $t ${t.id}")
                    promise.success()
                  }
                case None ⇒
                  model.console.error(s"Funds check fail. Transaction ${t} did not have a FROM." +
                    s"${t.id}")
                  promise.failure(new Exception("Transaction did not have a parent"))
              }
            })
        }.recover {
          case e: Throwable ⇒
            model.console.error("Operational exception while trying to check funds")
            promise.failure(new Exception("Operational exception while trying to check funds"))
        }.get

      case v: Vote ⇒
        model.console.trace(s"Funds check on vote $connection")
        Try {
          // the dataloaders must be present
            v.whenLoadComplete foreach (_ ⇒ {
              println(s"VOTE CONS ${v.sc.all} ${v.rx.cons}")
              println(s"PARENT ${v.parentAnswer}")
              println(s"LOAD STATUS ${v.whenLoadComplete.map { _ ⇒ true}.toRx(false)}")
              v.parentAnswer.now match {
                case Some(c) ⇒ val w = new Wallet(hub, c)
                  if (w.getUsableVotes.now - Math.abs(v.getAmount.now.get) >= 0) {
                    promise.success()
                  } else {
                    model.console.error("Not enough voting power!")
                    model.console.println(s"User had following connections ${hub.sc.all} | \n ${hub.rx.cons}")
                    promise.failure(new NotEnoughVotingPower(hub))
                  }
                case None ⇒ promise.failure(new Exception("Vote did not have a parent"))
              }
            })
        }.recover {
          case e: Throwable ⇒
            model.console.error("Operational exception while trying to check votes")
            promise.failure(new Exception("Operational exception while trying to check funds (votes)"))
        }.get

      case _ ⇒
        model.console.trace(s"Funds check on $connection. Auto pass.")
        promise.success()
    }

    promise.future
  }
}
