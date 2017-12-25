package life.plenty.model

trait Node {
  val id: String
}

case class EmptyNode(id: String) extends Node

case class Data[T](data: T) extends Node {
  override val id = data.toString
}

