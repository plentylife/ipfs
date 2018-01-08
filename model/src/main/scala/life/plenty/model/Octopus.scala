package life.plenty.model

import life.plenty.model.MarkerEnum.MarkerEnum

trait Octopus {
  val partialId: String = ""
  protected var _modules: List[Module[Octopus]] = ModuleRegistry.getModules(this)
  //  val mandatoryConnections: Set[Class[Connection[_]]]
  protected var _connections: List[Connection[_]] = List()

  def id: String = ???

  def modules: List[Module[Octopus]] = _modules

  def getTopModule[T <: Module[Octopus]](matchBy: PartialFunction[Module[Octopus], T]): Option[T] =
    _modules.collectFirst(matchBy)

  def getModules[T <: Module[Octopus]](matchBy: PartialFunction[Module[Octopus], T]): List[T] =
    _modules.collect(matchBy).reverse
  def addModule(module: Module[Octopus]): Unit = _modules = module :: _modules

  def getTopConnection[T](f: PartialFunction[Connection[_], Connection[T]]): Option[Connection[T]] =
    connections.collectFirst(f)

  def getTopConnectionData[T](f: PartialFunction[Connection[_], T]): Option[T] =
    connections.collectFirst(f)

  def hasMarker(marker: MarkerEnum): Boolean = connections.collect { case Marker(m) if m == marker ⇒ true } contains true

  def connections: List[Connection[_]] = _connections
  def addConnection(connection: Connection[_]): Either[Exception, Unit] = {
    modules.collectFirst { case m: ActionOnGraphTransform ⇒ m.onConnectionAdd(connection) } match {
      case None ⇒
        _connections = connection :: _connections
        Right()
      case Some(r) ⇒ r
    }
  }

  protected def preConstructor(): Unit = Unit

  /* Constructor */
  preConstructor()
  println("Octopus constructor")
  getModules({ case m: ActionOnInitialize[_] ⇒ m }).foreach({_.onInitialize()})
  //  println(connections)

}

trait Connection[T] {
  val value: T

  def id: String = value.hashCode().toBinaryString
}

case class Parent[T <: Octopus](parent: T) extends Connection[T] {
  override val value: T = parent
}

case class Child[T <: Octopus](child: T) extends Connection[T] {
  override val value = child
}

case class Creator[String](user: String) extends Connection[String] {
  override val value: String = user
}

case class CreationTime[Long](time: Long) extends Connection[Long] {
  override val value: Long = time
}

case class Title(title: String) extends Connection[String] {
  override val value: String = title
}

case class Marker(m: MarkerEnum) extends Connection[MarkerEnum] {
  override val value: MarkerEnum = m
}

object MarkerEnum extends Enumeration {
  type MarkerEnum = Value
  val FILL_GREAT_QUESTIONS, HAS_FILLED_GREAT_QUESTIONS = Value
}

trait Module[+T <: Octopus] {
  val withinOctopus: T
}

trait ActionOnGraphTransform extends Module[Octopus] {
  def onConnectionAdd(connection: Connection[_]): Either[Exception, Unit]

  def onConnectionRemove(connection: Connection[_]): Either[Exception, Unit]
}

trait ActionOnInitialize[T <: Octopus] extends Module[T] {
  def onInitialize()
}


trait Question extends Space with WithParent[Space]

trait GreatQuestion extends Question

trait Answer extends Octopus