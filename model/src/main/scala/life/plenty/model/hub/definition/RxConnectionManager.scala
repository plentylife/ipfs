package life.plenty.model.hub.definition

import life.plenty.model
import life.plenty.model.RxConsList
import life.plenty.model.connection.DataHub
import life.plenty.model.modifiers.RxConnectionFilters
import rx.opmacros.Utils.Id
import rx.{Ctx, Rx, Var}
import rx.async._
import rx.async.Platform._

import scala.concurrent.duration._

trait RxConnectionManager {
  self: Hub ⇒

  // this is now just a stub

  object rx {

    def cons(implicit ctx: Ctx.Owner): RxConsList = {
      Rx {sc.lazyAll}
    }

    /** @return first found non empty rx */
    def get[T](f: PartialFunction[DataHub[_], T])(implicit ctx: Ctx.Owner): Rx[Option[T]] = {
      Rx {sc.ex(f)}
    }

    def getAll[T](f: PartialFunction[DataHub[_], T])(implicit ctx: Ctx.Owner): Rx[List[T]] = {
      Rx {sc.exList(f)}
    }
  }

}
