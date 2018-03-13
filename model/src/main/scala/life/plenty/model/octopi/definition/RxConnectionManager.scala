package life.plenty.model.octopi.definition

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
  protected lazy val _connectionsRx: Var[List[Rx[Option[DataHub[_]]]]] =
    Var(List.empty[Rx[Option[DataHub[_]]]])
  protected lazy val _last: Var[DataHub[_]] =
    Var(null)

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
    _last() = connection
  })

  object rx {
    val debounceDuration = 1000 milliseconds

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

    def getAll[T](f: PartialFunction[DataHub[_], T])(implicit ctx: Ctx.Owner): Rx[List[T]] = {
      onConnectionsRequest.foreach(f ⇒ f())
      Rx {
        val raw = Lazy.getAll(f)
        raw() flatMap {rx ⇒ rx()}
      }.debounce(debounceDuration)
    }

    def getAllRaw[T](f: PartialFunction[DataHub[_], T])(implicit ctx: Ctx.Owner) = {
      onConnectionsRequest.foreach(f ⇒ f())
      Lazy.getAll(f)
    }

    object Lazy {
      def lazyCons(implicit ctx: Ctx.Owner): RxConsList = toRxConsList(_connectionsRx)

      def lazyGet[T](f: PartialFunction[DataHub[_], T])(implicit ctx: Ctx.Owner): Rx[Option[T]] =
        lazyCons(ctx) map {_.collectFirst(f)}

      def getAll[T](f: PartialFunction[DataHub[_], T])(implicit ctx: Ctx.Owner):
      Rx[List[Rx[Option[T]]]] = {
        if (_connections.now.length != _connectionsRx.now.length) {
          model.console.error("Rx Connection manager has different sizes of lists" +
            s"${_connections.now.length} ${_connectionsRx.now.length}")
          throw new Exception("rx.getAll has different sized lists")
        }
        // tail is important because the head gets processed next
        val initial: List[Rx[Option[T]]] = _connections.now.tail zip _connectionsRx.now.tail collect {
          case (s, rx) if f isDefinedAt s ⇒ rx map {_ map f} debounce(debounceDuration)
        }

        var add = _last.now == null
        val headRx: Rx[DataHub[_]] = _last.filter(h => h != null && (f isDefinedAt h)).debounce(debounceDuration)

        headRx.fold(initial)((list, e) ⇒ {
          val cs = _connectionsRx.debounce(debounceDuration)
          println(s"OCMP ${cs().head} --> ${e}")
          val h = cs().head.debounce(debounceDuration) map {_ collect f}
          // so we don't add the head twice
          if (_last.now != null && add) {
            h :: list
          } else {
            add = true
            list
          }
        }).debounce(debounceDuration)

      }
    }

  }

}
