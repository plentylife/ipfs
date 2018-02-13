package life.plenty.model.octopi.definition

import life.plenty.model.actions._
import life.plenty.model.connection.MarkerEnum.MarkerEnum
import life.plenty.model.connection.{Connection, Marker, NoMarker, TmpMarker}
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

  /** easy hook for external code */
  var tmpMarker: TmpMarker = NoMarker

  /** must be filled before accessed */
  protected val modulesFinishedLoading = Var(false)

  /* Constructor */
  _modules = ModuleRegistry.getModules(this)
  addOnConnectionRequestFunctions(
    getModules({ case m: ActionOnConnectionsRequest ⇒ m }).map(m ⇒ m.onConnectionsRequest _)
  )
  console.trace(s"Loaded modules ${_modules}")
  modulesFinishedLoading() = true
}

sealed trait TmpMarker

object NoMarker extends TmpMarker

object AtInstantiation extends TmpMarker
