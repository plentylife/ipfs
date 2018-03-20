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
  private[GraphOp] var _produced: List[GraphOp[_]] = List()
  def produce[P](graphOp: GraphOp[P]): GraphOp[P] = {
    _produced = graphOp :: _produced
    graphOp
    }
}

object GraphOp {
  implicit class GraphOps[T](op: GraphOp[T]) {
    def collect[R](f: PartialFunction[T, R]): Option[GraphOp[R]] = {
      Option(op.value).collect(f) map {v ⇒
        op match {
          case i @ Insert(_) ⇒ i.produce(Insert(v))
          case r @ Remove(_) ⇒ r.produce(Remove(v))
        }
      }
    }
  }
  implicit class GraphOpsStream[T](stream: Observable[GraphOp[T]]) {
    def collectOps[R](f: PartialFunction[T, R]): Observable[GraphOp[R]] =
      stream.map(_.collect(f)).collect({case Some(op) ⇒ op})

    def depMap[M](operation: T ⇒ Observable[M]): Observable[GraphOp[M]] = {
      val branches = stream.collect({case Insert(e) ⇒ e}).map { elem ⇒
        val depObs = operation(elem)
        val in = depObs.map(Insert(_))
        val out = stream.collect({case Remove(elem) ⇒ in.map(op ⇒
          Remove(op.value)
        )}).flatten

        Observable.concat(in, out)
      }

      val single: Observable[GraphOp[M]] = branches.flatten
      println("FLATMAP")
      println(branches)
      single.dump("FM").subscribe()
      println("--")

      single
    }

    def strip = stream.map(_.value)

    def asBoolean = stream.map({
      case Remove(_) ⇒ false
      case Insert(_) ⇒ true
    })

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
