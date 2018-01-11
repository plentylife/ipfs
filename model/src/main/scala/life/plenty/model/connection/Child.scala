package life.plenty.model.connection

import life.plenty.model.Octopus

case class Child[T <: Octopus](child: T) extends Connection[T] {
  override def value = child
}
