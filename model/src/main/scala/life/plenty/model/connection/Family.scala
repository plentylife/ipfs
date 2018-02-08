package life.plenty.model.connection

import life.plenty.model.octopi.definition.Octopus

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

case class Created[T <: Octopus](created: T) extends Connection[T] {
  override def value: T = created
}

object Created extends InstantiateFromOctopusByApply[Created[_]] {
  override def instantiate(from: Octopus): Created[_] = {
    Created(from)
  }
}
