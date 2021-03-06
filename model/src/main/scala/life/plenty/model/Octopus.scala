package life.plenty.model

import life.plenty.model.actions.{ActionAfterGraphTransform, ActionOnGraphTransform, ActionOnInitialize}
import life.plenty.model.connection.MarkerEnum.MarkerEnum
import life.plenty.model.connection.{Connection, Marker}
import life.plenty.model.modifiers.ModuleFilters

trait Octopus {
  val partialId: String = ""
  protected var _modules: List[Module[Octopus]] = List()
  //  val mandatoryConnections: Set[Class[Connection[_]]]
  protected var _connections: List[Connection[_]] = List()

  def id: String = ???

  private lazy val moduleFilters = getAllModules({ case m: ModuleFilters[_] ⇒ m })

  def modules: List[Module[Octopus]] = {
    println("mod filters of", this)
    println(moduleFilters)
    moduleFilters.foldLeft(_modules)((ms, f) ⇒ {
      //      println("filter stage", ms, f)
      f(ms)
    })
  }

  def getTopModule[T <: Module[Octopus]](matchBy: PartialFunction[Module[Octopus], T]): Option[T] = {
    //    println("getting top module", modules)
    modules.collectFirst(matchBy)
  }

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
  def addModule(module: Module[Octopus]): Unit = _modules = module :: _modules

  def getTopConnection[T](f: PartialFunction[Connection[_], Connection[T]]): Option[Connection[T]] =
    connections.collectFirst(f)

  def getTopConnectionData[T](f: PartialFunction[Connection[_], T]): Option[T] =
    connections.collectFirst(f)

  def hasMarker(marker: MarkerEnum): Boolean = connections.collect { case Marker(m) if m == marker ⇒ true } contains true

  def connections: List[Connection[_]] = _connections

  /** these modules are filtered */
  def getModules[T <: Module[Octopus]](matchBy: PartialFunction[Module[Octopus], T]): List[T] =
    modules.collect(matchBy)

  /** these modules do not have any filters applied */
  def getAllModules[T <: Module[Octopus]](matchBy: PartialFunction[Module[Octopus], T]): List[T] =
    _modules.collect(matchBy)

  protected def preConstructor(): Unit = Unit

  /* Constructor */
  println("Octopus pre-constructor -- ")
  println(this)
  println(this.getClass)

  _modules = ModuleRegistry.getModules(this)
  preConstructor()
  println("Octopus constructor -- " + this.toString)
  getModules({ case m: ActionOnInitialize[_] ⇒ m }).foreach({_.onInitialize()})
  //  println(connections)

}

trait Module[+T <: Octopus] {
  val withinOctopus: T
}