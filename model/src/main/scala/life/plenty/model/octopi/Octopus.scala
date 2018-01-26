package life.plenty.model.octopi

import life.plenty.model.ModuleRegistry
import life.plenty.model.actions._
import life.plenty.model.connection.MarkerEnum.MarkerEnum
import life.plenty.model.connection.{Connection, Marker}
import life.plenty.model.modifiers.{ConnectionFilters, ModuleFilters}
import rx.{Ctx, Rx, Var}

trait Octopus extends OctopusConstructor {
  implicit var ctx: Ctx.Owner = Ctx.Owner.safe()

  protected var _modules: List[Module[Octopus]] = List()
  protected lazy val _lastAddedConnection: Var[Option[Connection[_]]] = Var(None)
  protected lazy val _connections: Rx.Dynamic[List[Connection[_]]] =
    _lastAddedConnection.fold(List.empty[Connection[_]])(
      (list: List[Connection[_]], elem: Option[Connection[_]]) ⇒ {elem.map(_ :: list).getOrElse(list)})

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
  private lazy val connectionFilters = getAllModules({ case m: ConnectionFilters[_] ⇒ m })

  /** filters applied */
  def connections: List[Connection[_]] = {
    connectionFilters.foldLeft(_connections.now)((cs, f) ⇒ f(cs))
  }

  /** no filters applied */
  def allConnections: List[Connection[_]] = _connections.now

  def getTopConnection[T](f: PartialFunction[Connection[_], Connection[T]]): Option[Connection[T]] =
    connections.collectFirst(f)

  def getTopConnectionData[T](f: PartialFunction[Connection[_], T]): Option[T] =
    connections.collectFirst(f)

  @deprecated
  /** this method will go away as rx is introduced thoroughly.
    * does not filter. collects on raw connections list */
  def getAllTopConnectionDataRx[T](f: PartialFunction[Connection[_], T])(implicit ctx: Ctx.Owner): Rx[Option[T]] =
    _connections.now.collectFirst(f) match {
      case Some(c) ⇒ Var(Option(c))
      case None ⇒ _lastAddedConnection.map(_.collect(f))
    }

  object s {
    def get[T](f: PartialFunction[Connection[_], Connection[T]]): Option[Connection[T]] = getTopConnection(f)

    def ex[T](f: PartialFunction[Connection[_], T]): Option[T] = getTopConnectionData(f)

    def exf[T](f: PartialFunction[Connection[_], T]): T = ex(f).get
  }

  object rx {
    // fixme add filters
    def cons: Rx.Dynamic[List[Connection[_]]] = _connections map { cons ⇒
      connectionFilters.foldLeft(cons)((cs, f) ⇒ f(cs))
    }

    def get[T](f: PartialFunction[Connection[_], T])(implicit ctx: Ctx.Owner): Rx[Option[T]] =
      cons.now.collectFirst(f) match {
        case Some(c) ⇒ Var(Option(c))
        case None ⇒ getWatch(f)
      }

    def getWatch[T](f: PartialFunction[Connection[_], T])(implicit ctx: Ctx.Owner): Rx[Option[T]] = {
      _lastAddedConnection.map(_.collect(f))
    }

    //    def getAll[T <: Connection[_]](implicit ctx: Ctx.Owner): Rx[Iterable[T]] = _connections.
  }

  def hasMarker(marker: MarkerEnum): Boolean = connections.collect { case Marker(m) if m == marker ⇒ true } contains true

  def addConnection(connection: Connection[_]): Either[Exception, Unit] = {
    // duplicates are silently dropped
    //    println(s"adding connection ${connection} to ${this}")
    if (_connections.now.exists(_.id == connection.id)) return Right()

    var onErrorList = Stream(getModules({ case m: ActionOnGraphTransform ⇒ m }): _*) map { m ⇒
      m.onConnectionAdd(connection)
    }

    onErrorList.collectFirst({ case e: Left[Exception, Unit] ⇒ e }) match {
      case Some(e) ⇒ e

      case None ⇒
        //        _connections() = connection :: _connections.now
        _lastAddedConnection() = Option(connection)
        //        println(s"added connection ${connection} to ${this} ${_connections.now}")

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
  println("Octopus constructor -- " + this.getClass)
  getModules({ case m: ActionOnInitialize[_] ⇒ m }).foreach({_.onInitialize()})
  //  println(connections)

}


trait Module[+T <: Octopus] {
  val withinOctopus: T
}