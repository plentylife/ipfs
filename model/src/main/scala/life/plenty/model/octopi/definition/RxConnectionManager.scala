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

  onConnectionAddedOperation(connection ⇒ {
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

    object Lazy {
      def lazyCons(implicit ctx: Ctx.Owner): RxConsList = toRxConsList(_connectionsRx)

      def lazyGet[T](f: PartialFunction[DataHub[_], T])(implicit ctx: Ctx.Owner): Rx[Option[T]] =
        lazyCons(ctx) map {_.collectFirst(f)}

      def getAll[T](f: PartialFunction[DataHub[_], T])(implicit ctx: Ctx.Owner): Rx[List[T]] = {
        // todo. this can be optimized with getWatch
//                _connectionsRx.map(_ map { rx ⇒
//                  rx.map({ opt ⇒ opt.collect(f) })(ctx)
//                } flatMap { rx ⇒ rx() })(ctx)

          val initialIndicies = _connections.now.zipWithIndex.collect({
            case (dh, i) if f.isDefinedAt(dh) ⇒ i
          })

          val initial = initialIndicies.map(i ⇒
            _connectionsRx.now(i) map {_ map {v ⇒

              try {
                f(v)
            } catch {
              case e: Throwable ⇒
                model.console.error(s"Failed on getAll initial.")
                model.console.error(e)
              //            throw e
            }

            }})


          Rx {
            val list: Var[List[Rx[Option[Any]]]] = Var(initial)
            if (f.isDefinedAt(_connections().head)) {
              try {
                list() = (_connectionsRx().head map {_ map {v ⇒

                  try {
                    f(v)
                  } catch {
                    case e: Throwable ⇒
                      println("CONTINUOUS ERROR")
                      e.printStackTrace()
                      throw e;
                  }

                }}) :: list.now
              } catch {
                case e: Throwable ⇒
                  model.console.error(s"Failed on partial function. ${_connections().head} " +
                    s"${_connectionsRx().head}")
                  model.console.error(e)
              }
            }
            list() flatMap { rx ⇒ rx() }
          }


      }
    }

  }

}
