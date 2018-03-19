package life.plenty.model.hub.definition

import life.plenty.model.connection.{Active, DataHub, Inactive}
import monix.execution.CancelableFuture
import monix.reactive.{MulticastStrategy, Observable}
import monix.reactive.OverflowStrategy.Unbounded
import monix.reactive.observers.Subscriber
import monix.execution.cancelables.SingleAssignCancelable
import monix.execution.Scheduler.Implicits.global

import scala.concurrent.Future

sealed trait GraphOp[T] {
  val connection: DataHub[T]
}
case class Insert[T](connection: DataHub[T]) extends GraphOp[T]
case class Remove[T](connection: DataHub[T]) extends GraphOp[T]


trait ConnectionFeed {self: ConnectionManager ⇒
  val (feedSub, feed) = Observable.multicast[GraphOp[_]](MulticastStrategy.publish)

  def onInsert(con: DataHub[_]): Unit = {
    if (con.isActive) {
      feedSub.onNext(Insert(con))
    }
    con.isRemoved.foreach {r ⇒
      val op = if (r) Remove(con) else Insert(con)
      feedSub.onNext(op)
    }
  }

  def getStream: Observable[GraphOp[_]] = Observable.fromIterable(connections map {Insert(_)}) ++ feed

  val isRemoved = feed.collect {
    case Insert(Inactive(_)) ⇒ -1
    case Insert(Active(_)) ⇒ 1
  }.scan(0)((s, ai) ⇒ s + ai).map(_ < 0)
}
