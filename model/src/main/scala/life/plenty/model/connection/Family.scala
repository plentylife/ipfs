package life.plenty.model.connection

import life.plenty.model.octopi.Octopus

case class Child[T <: Octopus](child: T) extends Connection[T] {
  override def value = child
}

object Child extends InstantiateFromOctopusByApply[Child[_]] {
  override def instantiate(from: Octopus): Child[_] = Child(from)
}

case class Parent[T <: Octopus](parent: T) extends Connection[T] {
  override def value: T = parent
}

object Parent extends InstantiateFromOctopusByApply[Parent[_]] {
  override def instantiate(from: Octopus): Parent[_] = {
    Parent(from)
  }
}
