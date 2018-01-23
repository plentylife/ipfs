package life.plenty.model.octopi

import life.plenty.model.ModuleRegistry
import life.plenty.model.actions.{ActionAfterGraphTransform, ActionOnAddToModuleStack, ActionOnGraphTransform,
  ActionOnInitialize}
import life.plenty.model.connection.MarkerEnum.MarkerEnum
import life.plenty.model.connection.{Connection, Id, Marker}
import life.plenty.model.modifiers.{ConnectionFilters, ModuleFilters}

trait Octopus {
  val _id: String = null
  val idProperty = new Property({ case Id(idValue: String) ⇒ idValue }, this, Option(_id))

  protected var _modules: List[Module[Octopus]] = List()
  //  val mandatoryConnections: Set[Class[Connection[_]]]
  protected var _connections: List[Connection[_]] = List()

  def id: String = idProperty getOrElse idGenerator

  def idGenerator: String = ???

  private lazy val moduleFilters = getAllModules({ case m: ModuleFilters[_] ⇒ m })

  def modules: List[Module[Octopus]] = {
    moduleFilters.foldLeft(_modules)((ms, f) ⇒ {
      f(ms)
    })
  }

  /** these modules are filtered */
  def getModules[T <: Module[Octopus]](matchBy: PartialFunction[Module[Octopus], T]): List[T] =
    modules.collect(matchBy)

  /** these modules do not have any filters applied */
  def getAllModules[T <: Module[Octopus]](matchBy: PartialFunction[Module[Octopus], T]): List[T] =
    _modules.collect(matchBy)

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
  private lazy val connectionFilters = getAllModules({ case m: ConnectionFilters[_] ⇒ m })

  /** filters applied */
  def connections: List[Connection[_]] = {
    //    println("con fitlres", connectionFilters)
    connectionFilters.foldLeft(_connections)((cs, f) ⇒ f(cs))
  }

  /** no filters applied */
  def allConnections: List[Connection[_]] = _connections

  def getTopConnection[T](f: PartialFunction[Connection[_], Connection[T]]): Option[Connection[T]] =
    connections.collectFirst(f)

  def getTopConnectionData[T](f: PartialFunction[Connection[_], T]): Option[T] =
    connections.collectFirst(f)

  def hasMarker(marker: MarkerEnum): Boolean = connections.collect { case Marker(m) if m == marker ⇒ true } contains true


  def addConnection(connection: Connection[_]): Either[Exception, Unit] = {
    var onErrorList = Stream(getModules({ case m: ActionOnGraphTransform ⇒ m }): _*) map { m ⇒
      m.onConnectionAdd(connection)
    }

    onErrorList.collectFirst({ case e: Left[Exception, Unit] ⇒ e }) match {
      case Some(e) ⇒ e

      case None ⇒
        _connections = connection :: _connections
        onErrorList = Stream(getModules({ case m: ActionAfterGraphTransform ⇒ m }): _*) map { m ⇒
          m.onConnectionAdd(connection)
        }
        onErrorList.collectFirst({ case e: Left[Exception, Unit] ⇒ e }) match {
          case Some(e) ⇒ e
          case None ⇒ Right()
        }
    }
  }

  /* Constructor */

  /** this is ran before the modules are registered */
  protected def preConstructor(): Unit = Unit

  /* Constructor */
  _modules = ModuleRegistry.getModules(this)
  preConstructor()
  println("Octopus constructor -- " + this.toString)
  getModules({ case m: ActionOnInitialize[_] ⇒ m }).foreach({_.onInitialize()})
  //  println(connections)

}

/** the get on connection data is not safe */
class Property[T](getter: PartialFunction[Connection[_], T], in: Octopus, private val init: Option[T] = None) {
  private var _init: Option[T] = init

  private[octopi] def initWith(v: T): Unit = _init = Option(v)

  def forInit(f: (T) ⇒ Unit): Unit = _init foreach f

  def apply(): T = _init getOrElse in.getTopConnectionData(getter).get

  def getSafe: Option[T] = _init orElse in.getTopConnectionData(getter)

  def map[B](f: (T) ⇒ B): Option[B] = getSafe map f

  def getOrElse(v: T): T = getSafe.getOrElse(v)
}

trait Module[+T <: Octopus] {
  val withinOctopus: T
}