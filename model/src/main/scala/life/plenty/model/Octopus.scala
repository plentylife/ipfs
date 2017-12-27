package life.plenty.model

trait Octopus {
  val partialId: String = ???
  protected var _modules: List[Module[_]] = ModuleRegistry.getModules(this)
  //  val mandatoryConnections: Set[Class[Connection[_]]]
  protected var _connections: List[Connection[_]] = List()

  def id: String = {???}

  def modules = _modules

  def connections: List[Connection[_]] = _connections

  def addModule(module: Module[_]) = _modules :+= module
  def addConnection(connection: Connection[_]): Either[Exception, Unit] = {
    modules.collectFirst { case m: ActionOnGraphTransform ⇒ m.onConnectionAdd(connection) } match {
      case None ⇒ Right()
      case Some(r) ⇒ r
    }
  }

}

trait Connection[T] {
  val id: String = value.hashCode().toBinaryString
  val value: T
}

case class Parent[T <: Octopus](parent: T) extends Connection[T] {
  override val value: T = parent
}

case class Creator[String](user: String) extends Connection[String] {
  override val value: String = user
}

case class CreationTime[Long](time: String) extends Connection[Long] {
  override val value: String = time
}

case class Title(title: String) extends Connection[String] {
  override val value: String = title
}


trait Module[+T <: Octopus] {
  val withinOctopus: T
}

trait ActionOnGraphTransform extends Module[Octopus] {
  def onConnectionAdd(connection: Connection[_]): Either[Exception, Unit]

  def onConnectionRemove(connection: Connection[_]): Either[Exception, Unit]
}

trait Space extends Octopus {
  val title: String

  _connections :+= Title(title)
}

trait Question extends Space

trait GreatQuestion extends Question

trait Answer extends Octopus