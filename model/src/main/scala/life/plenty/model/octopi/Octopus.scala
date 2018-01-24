package life.plenty.model.octopi

import life.plenty.model
import life.plenty.model.ModuleRegistry
import life.plenty.model.actions._
import life.plenty.model.connection.MarkerEnum.MarkerEnum
import life.plenty.model.connection.{Connection, Id, Marker}
import life.plenty.model.modifiers.{ConnectionFilters, ModuleFilters}

trait Octopus {
  val _id: String = null
  val idProperty = new Property[String]({ case Id(idValue: String) ⇒ idValue }, this, _id)

  protected var _modules: List[Module[Octopus]] = List()
  //  val mandatoryConnections: Set[Class[Connection[_]]]
  protected var _connections: List[Connection[_]] = List()

  //  def id: String = idProperty getOrElse base64.encodeToString(idGenerator.getBytes)
  def id: String = idProperty getOrLazyElse model.getHasher.b64(idGenerator)

  def idGenerator: String = {
    //    println(this, "is generating id", idProperty.getSafe, idProperty.getOrLazyElse("faulty"))
    ""
  }

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
    // duplicates are silently dropped
    if (_connections.exists(_.id == connection.id)) return Right()

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

/** the get on connection data is not safe
  *
  * @param init can be null */
class Property[T](val getter: PartialFunction[Connection[_], T], in: Octopus, val init: T = null) {
  private var _inner: Option[T] = Option(init)

  def setInner(v: T): Unit = _inner = Option(v)

  def applyInner(f: (T) ⇒ Unit): Unit = {
    //    println("trying to apply inner to ", _inner)
    _inner foreach f
    //    println("applied")
  }

  def apply(): T = {
    try {
      getSafe.get
    } catch {
      case e: Throwable ⇒ println(e.getMessage); e.printStackTrace(); throw e
    }
  }

  def getSafe: Option[T] = _inner orElse in.getTopConnectionData(getter)

  def map[B](f: (T) ⇒ B): Option[B] = getSafe map f

  def getOrLazyElse(v: ⇒ T): T = getSafe.getOrElse(v)

  private var updaters = Set[() ⇒ Unit]()

  def registerUpdater(f: () ⇒ Unit) = updaters += f

  def update(c: Connection[_]): Unit = {
    if (getter.isDefinedAt(c)) setInner(getter(c))
    updaters foreach (f ⇒ f())
  }

  /* Constructor */
  //  println("adding property watch module")
  in.addModule(new PropertyWatch[T](in, this))
}

trait Module[+T <: Octopus] {
  val withinOctopus: T
}