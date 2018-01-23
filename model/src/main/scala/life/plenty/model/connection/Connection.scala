package life.plenty.model.connection

import life.plenty.model.octopi.Octopus

trait Connection[T] {
  def value: T

  def id: String = (idGivenValue(value) + this.getClass.getSimpleName).hashCode().toBinaryString

  protected def idGivenValue(v: T) = v match {
    case o: Octopus ⇒ (o.id + "connection").hashCode().toBinaryString
    case other ⇒ other.hashCode().toBinaryString
  }
}

trait InstantiateFromStringByApply[T <: Connection[_]] {
  def instantiate(from: String): Option[T]

  def apply(className: String, from: String): Option[T] =
    if (className == this.getClass.getSimpleName) instantiate(from)
    else None
}