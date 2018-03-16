package life.plenty.model.hub.definition

import life.plenty.model.connection.{Active, DataHub, Inactive}
import monix.reactive.{MulticastStrategy, Observable}
import monix.reactive.OverflowStrategy.Unbounded
import monix.reactive.observers.Subscriber
import monix.execution.cancelables.SingleAssignCancelable
import monix.execution.Scheduler.Implicits.global

trait ConnectionFeed {self: ConnectionManager ⇒
  val (insertSub, inserts) = Observable.multicast[DataHub[_]](MulticastStrategy.replay)
  val (removeSub, removes) = Observable.multicast[DataHub[_]](MulticastStrategy.replay)

  def onInsert(con: DataHub[_]) = {

    insertSub.onNext(con)
  }

  val removed = inserts.collect {
    case Inactive(_) ⇒ -1
    case Active(_) ⇒ 1
  }.scan(0)((s, ai) ⇒ s + ai).map(_ < 0)
}
