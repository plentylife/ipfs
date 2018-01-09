package life.plenty.model.connection

import life.plenty.model.Octopus

case class Parent[T <: Octopus](parent: T) extends Connection[T] {
  override val value: T = parent
}
