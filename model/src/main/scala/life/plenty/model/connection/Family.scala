package life.plenty.model.connection

import life.plenty.model.octopi.definition.Hub

case class Child[T <: Hub](child: T) extends DataHub[T] {
  override def value = child
}

object Child extends InstantiateFromOctopusByApply[Child[_]] {
  override def instantiate(from: Hub): Child[_] = Child(from)
}

case class Parent[T <: Hub](parent: T) extends DataHub[T] {
  override def value: T = parent
}

object Parent extends InstantiateFromOctopusByApply[Parent[_]] {
  override def instantiate(from: Hub): Parent[_] = {
    Parent(from)
  }
}

case class RootParent[T <: Hub](parent: T) extends DataHub[T] {
  override def value: T = parent
}

object RootParent extends InstantiateFromOctopusByApply[RootParent[_]] {
  override def instantiate(from: Hub): RootParent[_] = RootParent(from)
}

case class Created[T <: Hub](created: T) extends DataHub[T] {
  override def value: T = created
}

object Created extends InstantiateFromOctopusByApply[Created[_]] {
  override def instantiate(from: Hub): Created[_] = {
    Created(from)
  }
}

case class Critical[T <: Hub](critical: T) extends DataHub[T] {
  override def value: T = critical
}

object Critical extends InstantiateFromOctopusByApply[Critical[_]] {
  override def instantiate(from: Hub): Critical[_] = {
    Critical(from)
  }
}
