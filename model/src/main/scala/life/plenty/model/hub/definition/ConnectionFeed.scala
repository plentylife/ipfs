package life.plenty.model.hub.definition

import life.plenty.model.connection.{Active, DataHub, Inactive}
import monix.execution.CancelableFuture
import monix.reactive.{MulticastStrategy, Observable}
import monix.reactive.OverflowStrategy.Unbounded
import monix.reactive.observers.Subscriber
import monix.execution.cancelables.SingleAssignCancelable
import monix.execution.Scheduler.Implicits.global

import scala.concurrent.Future

sealed trait GraphOp[+T] {
  val value: T
}

object GraphOp {
  implicit class GraphOps[T](op: GraphOp[T]) {
    def collect[R](f: PartialFunction[T, R]): Option[GraphOp[R]] = {
      Option(op.value).collect(f) map {v ⇒
        op match {
          case Insert(_) ⇒ Insert(v)
          case Remove(_) ⇒ Remove(v)
        }
      }
    }
  }
}

case class Insert[+T](value: T) extends GraphOp[T]
case class Remove[+T](value: T) extends GraphOp[T]



trait ConnectionFeed {self: ConnectionManager ⇒
  val (feedSub, feed) = Observable.multicast[GraphOp[DataHub[_]]](MulticastStrategy.publish)

  def onInsert(con: DataHub[_]): Unit = {
    if (con.isActive) {
      feedSub.onNext(Insert(con))
    }
    con.isRemoved.foreach {r ⇒
      val op = if (r) Remove(con) else Insert(con)
      feedSub.onNext(op)
    }
  }

  def getStream: Observable[GraphOp[DataHub[_]]] = {
    val existing: List[GraphOp[DataHub[_]]] = connections map {h ⇒ Insert(h : DataHub[_])}
    val existingObs: Observable[GraphOp[DataHub[_]]] = Observable.fromIterable(existing)
    existingObs ++ feed
  }
  def getStream[T](extractor: PartialFunction[DataHub[_], T]): Observable[GraphOp[T]] = {
    getStream.map(_.collect(extractor)).collect({case Some(op) ⇒ op})
  }
  def getInsertStream: Observable[DataHub[_]] = {
    Observable.fromIterable(connections) ++ feed.collect({case Insert(h) ⇒ h})
  }

  val isRemoved = feed.collect {
    case Insert(Inactive(_)) ⇒ -1
    case Insert(Active(_)) ⇒ 1
  }.scan(0)((s, ai) ⇒ s + ai).map(_ < 0)
}
