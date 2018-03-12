package life.plenty.model.octopi.definition

import java.util.Date

import life.plenty.model.actions._
import life.plenty.model.connection.MarkerEnum.MarkerEnum
import life.plenty.model.connection._
import life.plenty.model.modifiers.{ModuleFilters, RxConnectionFilters}
import life.plenty.model.octopi.User
import life.plenty.model.utils.GraphUtils
import life.plenty.model.{ModuleRegistry, console}
import rx.{Ctx, Rx, Var}

trait Hub extends OctopusConstructor with ConnectionManager[Any] with RxConnectionManager {
  implicit val ctx: Ctx.Owner = Ctx.Owner.safe()

  protected var _modules: List[Module[Hub]] = List()

  private lazy val moduleFilters = getAllModules({ case m: ModuleFilters[_] ⇒ m })

  def modules: List[Module[Hub]] = {
    //    console.trace(s"trying to get modules ${_modules} filters ${moduleFilters}")
    moduleFilters.foldLeft(_modules)((ms, f) ⇒ {
      f(ms)
    })
  }

  /** these modules are filtered */
  def getModules[T <: Module[Hub]](matchBy: PartialFunction[Module[Hub], T]): List[T] =
    modules.collect(matchBy)

  /** these modules do not have any filters applied */
  def getAllModules[T <: Module[Hub]](matchBy: PartialFunction[Module[Hub], T]): List[T] = {
    _modules.collect(matchBy)
  }

  def getTopModule[T <: Module[Hub]](matchBy: PartialFunction[Module[Hub], T]): Option[T] = {
    modules.collectFirst(matchBy)
  }

  def addModule(module: Module[Hub]): Unit = {
    _modules = module :: _modules
    module match {
      case m: ActionOnAddToModuleStack[_] ⇒ m.onAddToStack()
      case _ ⇒
    }
  }

  lazy val isActive = GraphUtils.isActive(this)
  def inactivate() = if (isActive.now) {
    val c = Inactive(new Date().getTime)
    c.asNew()
    addConnection(c)
  }
  def activate() = if (!isActive.now) {
    val c = Active(new Date().getTime)
    c.asNew()
    addConnection(c)
  }

  override def equals(o: Any): Boolean = o match {
    case that: Hub => that.id.equalsIgnoreCase(this.id)
    case _ => false
  }

  override def hashCode: Int = id.hashCode

  /** easy hook for external code */
  var tmpMarker: TmpMarker = NoMarker

  /** must be filled before accessed */
  protected val modulesFinishedLoading = Var(false)

  /* Constructor */
  _modules = ModuleRegistry.getModules(this)
  addOnConnectionRequestFunctions(
    getModules({ case m: ActionOnConnectionsRequest ⇒ m }).map(m ⇒ m.onConnectionsRequest _)
  )
  console.trace(s"Loaded modules in ${this.getClass.getSimpleName} ${_modules}")
  modulesFinishedLoading() = true
}

trait TmpMarker

object NoMarker extends TmpMarker

object AtInstantiation extends TmpMarker
