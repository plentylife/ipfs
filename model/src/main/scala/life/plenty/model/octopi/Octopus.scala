package life.plenty.model.octopi

import life.plenty.model.actions._
import life.plenty.model.connection.MarkerEnum.MarkerEnum
import life.plenty.model.connection.{Connection, Marker, Removed}
import life.plenty.model.modifiers.{ModuleFilters, RxConnectionFilters}
import life.plenty.model.{ModuleRegistry, console}
import rx.{Ctx, Rx, Var}

trait Octopus extends OctopusConstructor {
  implicit val ctx: Ctx.Owner = Ctx.Owner.safe()

  protected var _modules: List[Module[Octopus]] = List()
  protected lazy val _lastAddedConnection: Var[Rx[Option[Connection[_]]]] = Var(Var(None))
  protected lazy val _connections: Var[List[Connection[_]]] = Var(List.empty[Connection[_]])
  protected lazy val _connectionsRx: Var[List[Rx[Option[Connection[_]]]]] = Var(List.empty[Rx[Option[Connection[_]]]])

  private lazy val moduleFilters = getAllModules({ case m: ModuleFilters[_] ⇒ m })

  def modules: List[Module[Octopus]] = {
    //    console.trace(s"trying to get modules ${_modules} filters ${moduleFilters}")
    moduleFilters.foldLeft(_modules)((ms, f) ⇒ {
      f(ms)
    })
  }

  /** these modules are filtered */
  def getModules[T <: Module[Octopus]](matchBy: PartialFunction[Module[Octopus], T]): List[T] =
    modules.collect(matchBy)

  /** these modules do not have any filters applied */
  def getAllModules[T <: Module[Octopus]](matchBy: PartialFunction[Module[Octopus], T]): List[T] = {
    _modules.collect(matchBy)
  }

  def getTopModule[T <: Module[Octopus]](matchBy: PartialFunction[Module[Octopus], T]): Option[T] = {
    modules.collectFirst(matchBy)
  }

  def addModule(module: Module[Octopus]): Unit = {
    _modules = module :: _modules
    module match {
      case m: ActionOnAddToModuleStack[_] ⇒ m.onAddToStack()
      case _ ⇒
    }
  }

  /* Connections */
  private lazy val connectionFilters = getAllModules({ case m: RxConnectionFilters[_] ⇒ m })

  /** filters are no longer applied */
  @deprecated("This is not reliable due to the nature of gun loading")
  def connections: List[Connection[_]] = {
    allConnections
    //    connectionFilters.foldLeft(_connections.now)((cs, f) ⇒ f(cs))
  }

  /** no filters applied */
  def allConnections: List[Connection[_]] = _connections.now

  @deprecated("This is not reliable due to the nature of gun loading")
  def getTopConnection[T](f: PartialFunction[Connection[_], Connection[T]]): Option[Connection[T]] =
    connections.collectFirst(f)

  @deprecated("This is not reliable due to the nature of gun loading")
  def getTopConnectionData[T](f: PartialFunction[Connection[_], T]): Option[T] =
    connections.collectFirst(f)

  object s {
    def get[T](f: PartialFunction[Connection[_], Connection[T]]): Option[Connection[T]] = getTopConnection(f)

    def ex[T](f: PartialFunction[Connection[_], T]): Option[T] = getTopConnectionData(f)

    def exf[T](f: PartialFunction[Connection[_], T]): T = ex(f).get
  }

  object rx {
    type RxConsList = Rx[List[Connection[_]]]

    def toRxConsList(in: Var[scala.List[Rx[Option[Connection[_]]]]]): RxConsList = {
      //      println("rx cons list converter")
      //      try {
      //        println(in.now.headOption)
      //      } catch {
      //        case e: Throwable ⇒ println(s"rx cons list converter error ${e.getMessage}"); e.printStackTrace();
      // throw e
      //      }
      //      println("rx cons list converter after")
      in.map({ list ⇒
        //        println(s"converting toRxConsList ${list}")
        list.flatMap(rx ⇒ {
          rx()
        })
      })
    }

    def allCons(implicit ctx: Ctx.Owner): RxConsList = {
      console.trace(s"rx.allCons ${onConnectionsRequestedModules} ${_connections}")
      onConnectionsRequestedModules.foreach(_.onConnectionsRequest())
      _connections
    }

    def cons(implicit ctx: Ctx.Owner): RxConsList = {
      console.trace(s"rx.cons ${this.getClass} ${_connections} ${onConnectionsRequestedModules}")
      //      val test = _connectionsRx
      //      console.trace(s"rx.cons ${if (_connectionsRx != null) _connectionsRx else "NULL"} ${_connections}")
      onConnectionsRequestedModules.foreach(_.onConnectionsRequest())

      toRxConsList(_connectionsRx)
    }

    def get[T](f: PartialFunction[Connection[_], T])(implicit ctx: Ctx.Owner): Rx[Option[T]] = {
      val test = cons
      //      console.trace(s"aaa ${cons(ctx).isInstanceOf[Rx[_]]}")
      //      console.trace(s"aaa-bbb ${cons(ctx) != null}")
      //      console.trace(s"aaa-c ${_connectionsRx.kill}")
      //      console.trace(s"aaa-d ${_connectionsRx.now.map(_ ⇒ ".").mkString}")
      //      console.trace(s"aaa-list ${cons(ctx).now.isInstanceOf[List[_]]}")
      cons(ctx).now.collectFirst(f) match {
        case Some(c) ⇒ Var(Option(c))
        case None ⇒ getWatch(f)(ctx)
      }
    }

    /** todo. rename this function later on.
      * figuring out exactly why the bug of unregistered updates happens */
    def getContinious[T](f: PartialFunction[Connection[_], T])(implicit ctx: Ctx.Owner): Rx[Option[T]] = {
      cons(ctx) map {_.collectFirst(f)}
    }

    def getAll[T](f: PartialFunction[Connection[_], T])(implicit ctx: Ctx.Owner): Rx[List[T]] = {
      onConnectionsRequestedModules.foreach(_.onConnectionsRequest())
      Lazy.getAll(f)
    }

    def getWatch[T](f: PartialFunction[Connection[_], T])(implicit ctx: Ctx.Owner): Rx[Option[T]] = {
      _lastAddedConnection.map(rx ⇒ rx().collect(f))(ctx).filter(_.nonEmpty)(ctx)
      //      _lastAddedConnection.map(rx ⇒ rx().collect(f))(ctx)
    }

    object Lazy {
      def lazyCons(implicit ctx: Ctx.Owner): RxConsList = toRxConsList(_connectionsRx)

      def lazyGet[T](f: PartialFunction[Connection[_], T])(implicit ctx: Ctx.Owner): Rx[Option[T]] =
        lazyCons.now.collectFirst(f) match {
          case Some(c) ⇒ Var(Option(c))
          case None ⇒ getWatch(f)(ctx)
        }

      def getAll[T](f: PartialFunction[Connection[_], T])(implicit ctx: Ctx.Owner): Rx[List[T]] = {
        // todo. this can be optimized with getWatch
        _connectionsRx.map(_ map { rx ⇒
          rx.map({ opt ⇒ opt.collect(f) })(ctx)
        } flatMap { rx ⇒ rx() })(ctx)
      }
    }

    //    def getAll[T <: Connection[_]](implicit ctx: Ctx.Owner): Rx[Iterable[T]] = _connections.
  }

  def hasMarker(marker: MarkerEnum): Boolean = connections.collect { case Marker(m) if m == marker ⇒ true } contains true

  private lazy val actionsOnGraphTransform = Stream(getModules({ case m: ActionOnGraphTransform ⇒ m }): _*)
  private lazy val actionsAfterGraphTransfrom = Stream(getModules({ case m: ActionAfterGraphTransform ⇒ m }): _*)

  def addConnection(preliminaryConnection: Connection[_]): Either[Exception, Unit] = {
    console.trace(s"adding connection ${preliminaryConnection} to ${this.getClass.getSimpleName}")
    // dealing with the special case of removes
    var connection = preliminaryConnection
    // duplicates are silently dropped
    if (_connections.now.exists(_.id == connection.id)) {
      val idOfExistingRemoved = rx.toRxConsList(_connectionsRx).now
        .collectFirst({ case c@Removed(cid: String) if cid == connection.id ⇒ c.id })
      if (idOfExistingRemoved.nonEmpty) {
        console.trace(s"The existing `Removed` on ${connection} is being lifted $idOfExistingRemoved")
        connection = Removed(idOfExistingRemoved.get)
      } else {
        console.trace(s"Connection was not added since it exists ${connection}")
        return Right()
      }
    }

    var onErrorList = actionsOnGraphTransform map { m ⇒
      m.onConnectionAdd(connection)
    }

    onErrorList.collectFirst({ case e: Left[Exception, Unit] ⇒ e }) match {
      case Some(e) ⇒ e

      case None ⇒
        /*filtering block*/
        val filteredCon: Rx[Option[Connection[_]]] = connectionFilters.foldLeft[Rx[Option[Connection[_]]]](
          Var {Option(connection)}
        )((c, f) ⇒ f(c))
        _connectionsRx() = filteredCon :: (_connectionsRx.now: List[Rx[Option[Connection[_]]]])
        /* end block */
        _connections() = connection :: _connections.now
        _lastAddedConnection() = filteredCon
        //        println(s"added connection ${connection} to ${this} ${_connections.now}")

        onErrorList = actionsAfterGraphTransfrom map { m ⇒
          m.onConnectionAdd(connection)
        }
        onErrorList.collectFirst({ case e: Left[Exception, Unit] ⇒ e }) match {
          case Some(e) ⇒ e
          case None ⇒ Right()
        }
    }
  }

  def removeConnection(c: Connection[_]): Either[Exception, Unit] = {
    addConnection(Removed(c.id))
  }

  /** must be filled before accessed */
  private var onConnectionsRequestedModules: List[ActionOnConnectionsRequest] = null

  protected val modulesFinishedLoading = Var(false)

  /* Constructor */
  _modules = ModuleRegistry.getModules(this)
  onConnectionsRequestedModules = getModules({ case m: ActionOnConnectionsRequest ⇒ m })
  console.trace(s"Loaded modules ${_modules}")
  modulesFinishedLoading() = true
}


trait Module[+T <: Octopus] {
  val withinOctopus: T
}