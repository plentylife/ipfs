package life.plenty.model.utils

import life.plenty.model.connection.DataHub
import life.plenty.model.hub.definition.{Hub, Insert}
import life.plenty.model.utils.GraphUtils.collectDownTree
import monix.reactive.Observable
import rx.{Ctx, Rx}
import monix.execution.Scheduler.Implicits.global

import scala.concurrent.Future

object ObservableGraphUtils {
  def collectDownTree[T <: Hub](in: Hub, matchBy: PartialFunction[DataHub[_], T],
                                allowedPath: PartialFunction[DataHub[_],Hub]): Observable[T] = {
    val pathCons = in.getFeed.collect({case Insert(h) ⇒ h}).collect(allowedPath)
    val hubs = in.getFeed.collect({case Insert(h) ⇒ h}).collect(matchBy)

    val res = pathCons flatMap {h ⇒
      collectDownTree(h, matchBy, allowedPath)
    }
    hubs ++ res
  }
}
