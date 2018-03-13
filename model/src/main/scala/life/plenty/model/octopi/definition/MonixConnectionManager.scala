package life.plenty.model.octopi.definition

import life.plenty.model.connection.DataHub
import monix.execution.Cancelable
import monix.reactive.Observable
import monix.reactive.OverflowStrategy.Unbounded

//trait MonixConnectionManager {
//  self: Hub ⇒
//  protected lazy val observable = Observable.create(Unbounded) { subscriber =>
//    val c = SingleAssignmentCancelable()
//   c := execution.Cancelable(() => target.removeEventListener(event, f))
//  }
//
////  private lazy val connectionFilters = getAllModules({ case m: RxConnectionFilters[_] ⇒ m })
//
//  private var onConnectionsRequest: List[() ⇒ Unit] = List()
//
//  def addOnConnectionRequestFunctions(fList: List[() ⇒ Unit]): Unit = onConnectionsRequest :::= fList
//
//  onConnectionAddedOperation(connection ⇒ {
//    /*filtering block*/
////    val filteredCon: Rx[Option[DataHub[_]]] = connectionFilters.foldLeft[Rx[Option[DataHub[_]]]](
////      Var {Option(connection)}
////    )((c, f) ⇒ f(c))
////    _connectionsRx() = filteredCon :: (_connectionsRx.now: List[Rx[Option[DataHub[_]]]])
//    /* end block */
//
//
//  })
//
//  object monix {
//    def getAll[T](f: PartialFunction[DataHub[_], T])= {
//      onConnectionsRequest.foreach(f ⇒ f())
//      Lazy.getAll(f)
//    }
//
//    object Lazy {
//      def getAll[T](f: PartialFunction[DataHub[_], T]) = {
//        // todo. this can be optimized with getWatch
//        _connectionsRx.map(_ map { rx ⇒
//          rx.map({ opt ⇒ opt.collect(f) })(ctx)
//        } flatMap { rx ⇒ rx() })(ctx)
//      }
//    }
//
//  }
//
//}
