package life.plenty.model.utils

import life.plenty.model.connection.DataHub
import life.plenty.model.hub.definition.{Hub}
import life.plenty.model.utils.GraphUtils.collectDownTree
import monix.reactive.Observable
import rx.{Ctx, Rx}
import monix.execution.Scheduler.Implicits.global

import scala.concurrent.Future

object SimpleGraphUtils {
  def collectDownTree[T <: Hub](in: Hub, matchBy: PartialFunction[DataHub[_], T],
                                allowedPath: PartialFunction[DataHub[_],Hub]): Future[List[T]] = {
    val pathCons = in.conExList(allowedPath)
    val hubs = in.conExList(matchBy)
    val res = for (pc ← pathCons; hs ← hubs) yield {
      val down = pc map {p ⇒ collectDownTree(p, matchBy, allowedPath)}
      val downFlat = Future.sequence(down).map(_ reduce(_ ::: _))
      downFlat map {hs ::: _}
    }
    res.flatten
  }
}
