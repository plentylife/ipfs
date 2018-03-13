package life.plenty.model.octopi.definition

import life.plenty.model
import life.plenty.model.connection.DataHub
import life.plenty.model.modifiers.RxConnectionFilters
import rx.opmacros.Utils.Id
import rx.{Ctx, Rx, Var}

trait RxConnectionManager {
  self: Hub ⇒
  protected lazy val _connectionsRx: Var[List[Rx[Option[DataHub[_]]]]] = Var(List.empty[Rx[Option[DataHub[_]]]])

  private lazy val connectionFilters = getAllModules({ case m: RxConnectionFilters[_] ⇒ m })

  private var onConnectionsRequest: List[() ⇒ Unit] = List()

  def addOnConnectionRequestFunctions(fList: List[() ⇒ Unit]): Unit = onConnectionsRequest :::= fList

  onConnectionAddedOperation(connection ⇒ synchronized {
    /*filtering block*/
    val filteredCon: Rx[Option[DataHub[_]]] = connectionFilters.foldLeft[Rx[Option[DataHub[_]]]](
      Var {Option(connection)}
    )((c, f) ⇒ f(c))
    _connectionsRx() = filteredCon :: (_connectionsRx.now: List[Rx[Option[DataHub[_]]]])

    /* end block */
  })

  object rx {
    type RxConsList = Rx[List[DataHub[_]]]

    def toRxConsList(in: Var[scala.List[Rx[Option[DataHub[_]]]]])(implicit ctx: Ctx.Owner): RxConsList = {
      in.map({ list ⇒
        list.flatMap(rx ⇒ {rx()})
      })
    }

    def cons(implicit ctx: Ctx.Owner): RxConsList = {
      onConnectionsRequest.foreach(f ⇒ f())
      toRxConsList(_connectionsRx)
    }

    /** @return first found non empty rx */
    def get[T](f: PartialFunction[DataHub[_], T])(implicit ctx: Ctx.Owner): Rx[Option[T]] = {
      onConnectionsRequest.foreach(f ⇒ f())

      Rx {
        val rightRxIndex = _connections.map { list ⇒
          val processed = list.map(f.isDefinedAt)
          processed.indexOf(true) // if None, won't be found
        }
        val res: Rx[Option[T]] = rightRxIndex.flatMap { i ⇒
          if (i < 0) Var(None) else {
            val rxList = _connectionsRx()
            rxList(i) map { rx: Option[DataHub[_]] ⇒ rx collect f }
          }
        }

        res.foreach(r ⇒ if (r.nonEmpty) rightRxIndex.kill())
        res()
      }
    }

    //    def get[T](f: PartialFunction[DataHub[_], T])(implicit ctx: Ctx.Owner): Rx[Option[T]] = {
    //      cons(ctx) map {_.collectFirst(f)}
    //    }

    def getAll[T](f: PartialFunction[DataHub[_], T])(implicit ctx: Ctx.Owner): Rx[List[T]] = {
      onConnectionsRequest.foreach(f ⇒ f())
      Lazy.getAll(f)
    }

    def getAllRaw[T](f: PartialFunction[DataHub[_], T])(implicit ctx: Ctx.Owner): Rx[List[T]] = {
      onConnectionsRequest.foreach(f ⇒ f())
      Lazy.getAll(f)
    }

    object Lazy {
      def lazyCons(implicit ctx: Ctx.Owner): RxConsList = toRxConsList(_connectionsRx)

      def lazyGet[T](f: PartialFunction[DataHub[_], T])(implicit ctx: Ctx.Owner): Rx[Option[T]] =
        lazyCons(ctx) map {_.collectFirst(f)}

      def getAll[T](f: PartialFunction[DataHub[_], T])(implicit ctx: Ctx.Owner): Rx[List[T]] = {
        if (_connections.now.length != _connectionsRx.now.length) {
          println("ERROR: GETALL has different sizes")
          throw new Exception("GET ALL has different sizes")
        }
        val initial: List[Rx.Dynamic[Option[T]]] = _connections.now zip _connectionsRx.now collect {
          case (s, rx) if f isDefinedAt s ⇒ rx map {_ map f}
        }

        val headRx: Rx[Option[DataHub[_]]] = _connections.map(_.headOption)
          .filter(_ map { h ⇒ f isDefinedAt h} getOrElse false)

        Rx {
          var list = initial
          headRx foreach {_ ⇒
            val h = _connectionsRx().head map {_ collect f}
            list = h :: list
          }

          println(s"GETALL rx ${_connections.now}")

          list flatMap {rx ⇒ rx()}
        }

      }
    }

  }

}
