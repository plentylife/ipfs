package life.plenty.model.utils

import life.plenty.model.connection.DataHub
import life.plenty.model.hub.definition.{Hub, ObsStream}
import life.plenty.model.utils.GraphUtils.collectDownTree
import monix.reactive.Observable
import rx.{Ctx, Rx}
import monix.execution.Scheduler.Implicits.global

import scala.concurrent.Future

object ObservableGraphUtils {
  def collectDownTree[T <: Hub](in: Hub, matchBy: PartialFunction[DataHub[_], T],
                                allowedPath: PartialFunction[DataHub[_],Hub]): ObsStream[T] = {
    val pathCons = in.getStreamingList(allowedPath)
    pathCons._inserts.foreach(e ⇒
      println(s"PATH CONS ${e}")
    )
//    in.insertsC.toListL.foreach(l ⇒
//      println(s"PATH CONS Dir ${l}")
//    )
//    in.inserts.toListL.foreach(l ⇒
//      println(s"PATH CONS Dir ${l}")
//    )
    val hubs = in.getStreamingList(matchBy)
    def cdt(h: Hub) = collectDownTree(h, matchBy, allowedPath)
    val nextHubs = pathCons flatCombine cdt

    hubs ++ nextHubs

//    val pathCons = in.rx.getAll(allowedPath).debounce(debounceDuration millis)
//    val _hubs = in.rx.cons.debounce(debounceDuration millis)
//    val hubs = _hubs map {list ⇒
//      list collect matchBy
//    }
//
//    val nextHubs = pathCons() flatMap { h ⇒
//      val r = collectDownTree(h, matchBy, allowedPath)
//      r()
//    }
//
//    hubs() ::: nextHubs
//    ???
  }
}
