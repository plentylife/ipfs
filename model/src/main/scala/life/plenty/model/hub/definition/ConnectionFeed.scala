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

  }



//  implicit class DependencyStream()

}

case class Insert[+T](value: T) extends GraphOp[T]
case class Remove[+T](value: T) extends GraphOp[T]

class StateList[T](val stream: Observable[GraphOp[T]]) {
  private var state = List[T]()

  stream foreach {elem ⇒
    println(s"StateList foreach $elem")
    elem match {
    case Insert(i) ⇒ if (!state.contains(i)) state = i :: state
    case Remove(i) ⇒ state = state.filterNot(_ == i)
  }
  }

  def flatMap[M](op: T ⇒ Observable[M]): StateList[M] = {
    val inserts = state.map { elem ⇒
      val depObs = op(elem)
      elem → new StateList[M](depObs.map(Insert(_)))
    }

    val insertsMap = inserts.toMap
    val removals = stream.collect({
      case Remove(what) ⇒ what
    }).flatMap {r ⇒
      val depRem: List[Remove[M]] = insertsMap.get(r).map{ dep ⇒
        dep.state map {Remove(_)}
      }.getOrElse(List())
      Observable.fromIterable(depRem)
    }
    
    val insertObs = inserts.map(_._2.stream)
    val insertCon = Observable.concat(insertObs:_*)

    println("FLATMAP")
    println(state)
    println(inserts)
    insertCon.dump("FM").subscribe()
    println("--")

    new StateList[M](Observable.concat(removals, insertCon))
  }

//  def filter(op: T ⇒ Boolean): StateList[T] = {
//
//  }

  def get: Observable[GraphOp[T]] = Observable.fromIterable(state map { elem ⇒ Insert(elem)}) ++ stream
}



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
