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
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}

trait RxConnectionManager {
  self: Hub ⇒
  protected lazy val _connectionsRx: Var[List[Rx[Option[DataHub[_]]]]] =
    Var(List.empty[Rx[Option[DataHub[_]]]])
  protected lazy val _connectionsRxMap: Var[List[(DataHub[_], Rx[Option[DataHub[_]]])]] =
    Var(List())

  private lazy val connectionFilters = getAllModules({ case m: RxConnectionFilters[_] ⇒ m })

  private var onConnectionsRequest: List[() ⇒ Unit] = List()
  def addOnConnectionRequestFunctions(fList: List[() ⇒ Unit]): Unit = onConnectionsRequest :::= fList


  lazy val loadComplete = Promise[Unit]()
  /** triggers load */
  def loadCompleted: Future[Unit] = {
    onConnectionsRequest.foreach(f ⇒ f())
    loadComplete.future
  }
  lazy val loadCompletedRx = loadCompleted.map { _ ⇒ true}.toRx(false)



  onConnectionAddedOperation(connection ⇒ synchronized {
    /*filtering block*/
    val filteredCon: Rx[Option[DataHub[_]]] = connectionFilters.foldLeft[Rx[Option[DataHub[_]]]](
      Var {Option(connection)}
    )((c, f) ⇒ f(c))
    _connectionsRx() = filteredCon :: (_connectionsRx.now: List[Rx[Option[DataHub[_]]]])
    _connectionsRxMap() = (connection → filteredCon) :: _connectionsRxMap.now
    /* end block */
  })

  object rx {
    val debounceDuration = 1000 milliseconds

    def toRxConsList(in: Var[scala.List[Rx[Option[DataHub[_]]]]])(implicit ctx: Ctx.Owner): RxConsList = {
      in.map({ list ⇒
        list.flatMap(rx ⇒ {rx()})
      })
    }

    var lcFlag = false

    def onLoaded[T](whenLoadedDo: ⇒ Rx[T], default: T) = {

//      if (!lcFlag) {
//        loadCompletedRx foreach {status ⇒
//          println(s"LC ${self} ${status}")
//        }
//        lcFlag = true
//      }


        loadCompletedRx flatMap {isLoaded ⇒ if (isLoaded) {
        whenLoadedDo
      } else Rx {default}
      }
    }

    def cons(implicit ctx: Ctx.Owner): RxConsList = {
      onConnectionsRequest.foreach(f ⇒ f())
      toRxConsList(_connectionsRx)
    }

    /** @return first found non empty rx */
    def get[T](f: PartialFunction[DataHub[_], T])(implicit ctx: Ctx.Owner): Rx[Option[T]] = {
      onConnectionsRequest.foreach(f ⇒ f())

      def whenLoaded = {
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
        res
      }

      onLoaded(whenLoaded, None)
    }

    def getAll[T](f: PartialFunction[DataHub[_], T])(implicit ctx: Ctx.Owner): Rx[List[T]] = {
      onConnectionsRequest.foreach(f ⇒ f())
      def whenLoaded = Rx {
        val raw = Lazy.getAll(f)
        raw() flatMap {rx ⇒ rx()}
      }

      onLoaded(whenLoaded, List.empty[T])
    }

    def getAllRaw[T](f: PartialFunction[DataHub[_], T])(implicit ctx: Ctx.Owner) = {
      onConnectionsRequest.foreach(f ⇒ f())

      onLoaded(Lazy.getAll(f), List.empty)
    }

    object Lazy {
      def lazyCons(implicit ctx: Ctx.Owner): RxConsList = toRxConsList(_connectionsRx)

      def lazyGet[T](f: PartialFunction[DataHub[_], T])(implicit ctx: Ctx.Owner): Rx[Option[T]] =
        lazyCons(ctx) map {_.collectFirst(f)}

      def getAll[T](f: PartialFunction[DataHub[_], T])(implicit ctx: Ctx.Owner):
      Rx[List[Rx[Option[T]]]] = synchronized {

        _connectionsRxMap.debounce(debounceDuration).fold(List[Rx[Option[T]]]() → 0)((listWithMark, csMap) ⇒ {
          val take = csMap.length - listWithMark._2

          val list = csMap.take(take) collect {
            case (s, rx) if f isDefinedAt s ⇒ rx.debounce(debounceDuration) map {_ map f}
          }

          list → csMap.length
        }).debounce(debounceDuration).map(_._1)

      }
    }

  }

}
