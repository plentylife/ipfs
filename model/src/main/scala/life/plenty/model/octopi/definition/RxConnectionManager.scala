package life.plenty.model.octopi.definition

import life.plenty.model.actions.ActionOnConnectionsRequest
import life.plenty.model.connection.Connection
import life.plenty.model.console
import life.plenty.model.modifiers.RxConnectionFilters
import rx.{Ctx, Rx, Var}

trait RxConnectionManager {self: Octopus ⇒
  protected lazy val _connectionsRx: Var[List[Rx[Option[Connection[_]]]]] = Var(List.empty[Rx[Option[Connection[_]]]])

  private lazy val connectionFilters = getAllModules({ case m: RxConnectionFilters[_] ⇒ m })

  private var onConnectionsRequest: List[() ⇒ Unit] = List()

  def addOnConnectionRequestFunctions(fList: List[() ⇒ Unit]): Unit = onConnectionsRequest :::= fList

  // fixme add the filters

//  /*filtering block*/
//  val filteredCon: Rx[Option[Connection[_]]] = connectionFilters.foldLeft[Rx[Option[Connection[_]]]](
//    Var {Option(connection)}
//  )((c, f) ⇒ f(c))
//  _connectionsRx() = filteredCon :: (_connectionsRx.now: List[Rx[Option[Connection[_]]]])
//  /* end block */


  object rx {
    type RxConsList = Rx[List[Connection[_]]]

    def toRxConsList(in: Var[scala.List[Rx[Option[Connection[_]]]]])(implicit ctx: Ctx.Owner): RxConsList = {in.map({ list ⇒
        list.flatMap(rx ⇒ {rx()})})
    }

    def cons(implicit ctx: Ctx.Owner): RxConsList = {
      onConnectionsRequest.foreach(f ⇒ f())
      toRxConsList(_connectionsRx)
    }

    def get[T](f: PartialFunction[Connection[_], T])(implicit ctx: Ctx.Owner): Rx[Option[T]] = {
      cons(ctx) map {_.collectFirst(f)}
    }

    def getAll[T](f: PartialFunction[Connection[_], T])(implicit ctx: Ctx.Owner): Rx[List[T]] = {
      onConnectionsRequest.foreach(f ⇒ f())
      Lazy.getAll(f)
    }

    object Lazy {
      def lazyCons(implicit ctx: Ctx.Owner): RxConsList = toRxConsList(_connectionsRx)

      def lazyGet[T](f: PartialFunction[Connection[_], T])(implicit ctx: Ctx.Owner): Rx[Option[T]] =
        lazyCons(ctx) map {_.collectFirst(f)}

      def getAll[T](f: PartialFunction[Connection[_], T])(implicit ctx: Ctx.Owner): Rx[List[T]] = {
        // todo. this can be optimized with getWatch
        _connectionsRx.map(_ map { rx ⇒
          rx.map({ opt ⇒ opt.collect(f) })(ctx)
        } flatMap { rx ⇒ rx() })(ctx)
      }
    }
  }

}
