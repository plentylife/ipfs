package life.plenty.model.hub.definition

import life.plenty.model.connection.{Active, DataHub, Inactive}
import monix.eval.Coeval
import monix.execution.Scheduler.Implicits.global
import monix.reactive.{MulticastStrategy, Observable}

sealed trait GraphOp[+T] {
  val value: T
  private[GraphOp] var _produced: Set[GraphOp[_]] = Set()

  def produce[P](graphOp: GraphOp[P]): GraphOp[P] = {
    _produced += graphOp
    graphOp
  }

  def produced = _produced

  def clear() = {
    val p = _produced
    _produced = Set()
    p
  }
}

object GraphOp {
  type Feed[T] = Observable[GraphOp[T]]

  implicit class GraphOpSuperset[T](op: GraphOp[T]) {
    def collect[R](f: PartialFunction[T, R]): Option[GraphOp[R]] = {
      Option(op.value).collect(f) map { v ⇒
        op match {
          case i@Insert(_) ⇒ i.produce(Insert(v))
          case r@Remove(_) ⇒ r.produce(Remove(v))
        }
      }
    }
  }

  implicit class GraphOpsStream[T](feed: Observable[GraphOp[T]]) {
    def collectOps[R](f: PartialFunction[T, R]): Observable[GraphOp[R]] =
      feed.map(_.collect(f)).collect({ case Some(op) ⇒ op })

    def depMap[M](mapFunction: T ⇒ Observable[M]): Observable[GraphOp[M]] = {
      var lastInserts = Map[T, Observable[List[M]]]()

      val branches = feed.map {
        case op@Insert(elem) ⇒
          val depObs = mapFunction(op.value)
          val in = depObs.map(dep ⇒ op.produce(Insert(dep)))
          lastInserts += op.value → in.scan(List[M]()) { (list, sop) ⇒
            sop.value :: list
          }
          in
        case op@Remove(elem) ⇒
          lastInserts.get(elem).map(ins ⇒ {
            // get the last element of the scanned feed (all the elements depending on the Insert that pushed this elem
            val feed = ins.lastF.flatMap(list ⇒ Observable.fromIterable(list map { in ⇒ Remove(in) }))
            // remove those from memory
            lastInserts -= elem
            feed
          }) getOrElse Observable.empty[GraphOp[M]]
      }


      val single: Observable[GraphOp[M]] = branches.flatten
      println("FLATMAP")
      println(branches)
      single.dump("FM").subscribe()
      println("--")

      single
    }

    def depMapLast[M](mapFunction: T ⇒ Observable[M]): Observable[GraphOp[M]] = {
      var lastInserts = Map[T, M]()

      val branches: Observable[Observable[GraphOp[M]]] = feed.map {
        case op@Insert(elem) ⇒
          val depObs = mapFunction(op.value)
          val resObs = depObs.flatMap(dep ⇒ {
            // inserting the new value, and removing the old
            val in = Insert(dep)
            var obsList = List[GraphOp[M]](in)
            lastInserts.get(elem).foreach(lastDep ⇒ obsList :+= Remove(lastDep))
            // registering the latest dep elem
            lastInserts += op.value → dep
            Observable.fromIterable(obsList)
          })
          resObs.dump("DML RES").subscribe()
          depObs.dump("DML DEP").subscribe()
          resObs
        case op@Remove(elem) ⇒ lastInserts.get(elem).map(lastElem ⇒ {
          // get the last element of the scanned feed (all the elements depending on the Insert that pushed this elem
          val feed = Observable.coeval(Coeval(Remove(lastElem)))
          // remove those from memory
          lastInserts -= elem
          feed
        }) getOrElse Observable.empty[GraphOp[M]]
      }


      val single: Observable[GraphOp[M]] = branches.flatten
      println("DEP MAP LAST")
      single.dump("DML SINGLE").subscribe()
      println("--")

      single
    }

//    def mapToLastList[M](mapFunction: T ⇒ Observable[M]): Observable[]

    def strip = feed.map(_.value)

    def asBoolean = feed.map({
      case Remove(_) ⇒ false
      case Insert(_) ⇒ true
    })

    def scanToList: Observable[List[T]] = feed.scan(List.empty[T]){ (list, op) ⇒
      val res = op match {
        case Remove(what) ⇒ list diff List(what)
        case Insert(what) ⇒ what :: list
      }

      println(s"SCANED TO LIST $res")
      res
    }
  }

  def byType[T, R](op: GraphOp[T], ifInsert: T ⇒ R, ifRemove: T ⇒ R): R = {
    op match {
      case Remove(what) ⇒ ifRemove(what)
      case Insert(what) ⇒ ifInsert(what)
    }
  }

  implicit def numericOp[T : Numeric](op: GraphOp[T])(implicit num: Numeric[T]): T = {
    byType[T, T](op, {v ⇒ v}, {v ⇒ num.mkNumericOps(v).unary_-()})
  }

  //
  //  def collectLeafDependants(op: GraphOp[_]): Set[GraphOp[_]] = {
  //    val leaf = op.produced filter {_.produced.isEmpty}
  //    val node = op.produced flatMap {_.produced} flatMap collectLeafDependants
  //    leaf ++ node
  //  }
}

case class Insert[+T](value: T) extends GraphOp[T]

case class Remove[+T](value: T) extends GraphOp[T]


trait ConnectionFeed {
  self: ConnectionManager ⇒
  lazy val (feedSub, feed) = Observable.multicast[GraphOp[DataHub[_]]](MulticastStrategy.publish)

  var dumped = false

  def onInsert(con: DataHub[_]): Unit = {
    if (con.isActive) {
      val in = Insert(con)
      val ack = feedSub.onNext(in)
      println(s"INSERTED $this --> $con [$ack]")
      println(feed, feedSub)
    }

    con.loadCompleted foreach {_ ⇒
      con.isRemoved.foreach { r ⇒
        println(s"CHECKING IF SHOULD REMOVE $con")
        val op = if (r) {
          Remove(con)
        } else {
          Insert(con)
        }
        feedSub.onNext(op)
      }
    }

    if (!dumped) {
      dumped = true
      feed.dump(s"CONS IN ${this}").subscribe()
    }
  }

  def getFeed: Observable[GraphOp[DataHub[_]]] = {
    self.onConnectionsRequest.foreach(f ⇒ f())
    val existing: List[GraphOp[DataHub[_]]] = connections map { h ⇒ Insert(h: DataHub[_]) }
    println(s"EXISTING TO FEED $existing")
    val existingObs: Observable[GraphOp[DataHub[_]]] = Observable.fromIterable(existing)
    existingObs ++ feed
  }

  def getFeed[T](extractor: PartialFunction[DataHub[_], T]): Observable[GraphOp[T]] = {
    getFeed.map(_.collect(extractor)).collect({ case Some(op) ⇒ op })
  }

  def getInsertFeed: Observable[DataHub[_]] = {
    getFeed.collect({ case Insert(h) ⇒ h })
  }
  def getInsertFeed[T](extractor: PartialFunction[DataHub[_], T]): Observable[T]  = {
    getInsertFeed.collect(extractor)
  }

  val isRemoved = feed.collect {
    case Insert(Inactive(_)) ⇒ -1
    case Insert(Active(_)) ⇒ 1
  }.scan(0)((s, ai) ⇒ s + ai).map(_ < 0)
}
