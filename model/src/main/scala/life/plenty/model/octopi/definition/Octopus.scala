package life.plenty.model.octopi.definition

import life.plenty.model.actions._
import life.plenty.model.connection.MarkerEnum.MarkerEnum
import life.plenty.model.connection.{Connection, Marker, Removed}
import life.plenty.model.modifiers.{ModuleFilters, RxConnectionFilters}
import life.plenty.model.{ModuleRegistry, console}
import rx.{Ctx, Rx, Var}

trait Octopus extends OctopusConstructor with ConnectionManager[Any] with RxConnectionManager {
  implicit val ctx: Ctx.Owner = Ctx.Owner.safe()

  protected var _modules: List[Module[Octopus]] = List()

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

  def hasMarker(marker: MarkerEnum): Boolean = sc.all.collect { case Marker(m) if m == marker ⇒ true } contains true

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
      println(s"looking for existing ids in ${_connectionsRx.now}")
      if (idOfExistingRemoved.nonEmpty) {
        console.trace(s"The existing `Removed` on ${connection} is being lifted $idOfExistingRemoved")
        println(s"The existing `Removed` on ${connection} is being lifted $idOfExistingRemoved")
        connection = Removed(idOfExistingRemoved.get)
      } else {
        console.trace(s"Connection was not added since it exists ${connection}")
        println(s"Connection was not added since it exists ${connection}")
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
//  private var onConnectionsRequestedModules: List[ActionOnConnectionsRequest] = null

  protected val modulesFinishedLoading = Var(false)

  /* Constructor */
  _modules = ModuleRegistry.getModules(this)
  addOnConnectionRequestFunctions(
    getModules({ case m: ActionOnConnectionsRequest ⇒ m }).map(m ⇒ m.onConnectionsRequest _)
  )
  console.trace(s"Loaded modules ${_modules}")
  modulesFinishedLoading() = true
}