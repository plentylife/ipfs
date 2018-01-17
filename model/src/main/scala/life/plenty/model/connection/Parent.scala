package life.plenty.model.connection

import life.plenty.model.octopi.Octopus

case class Parent[T <: Octopus](parent: T) extends Connection[T] {
  override def value: T = parent
}
