package life.plenty.model.connection

import life.plenty.model.octopi.Octopus

trait Connection[T] {
  def value: T

  def id: String = idGivenValue(value)

  protected def idGivenValue(v: T) = v match {
    case o: Octopus ⇒ (o.id + "connection").hashCode().toBinaryString
    case other ⇒ other.hashCode().toBinaryString
  }
}