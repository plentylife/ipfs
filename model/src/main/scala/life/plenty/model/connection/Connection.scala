package life.plenty.model.connection

import life.plenty.model
import life.plenty.model.octopi.Octopus

trait Connection[T] {
  def value: T

  def id: String = idGivenValue(value) + this.getClass.getSimpleName

  protected def idGivenValue(v: T) = v match {
    case o: Octopus ⇒ model.getHasher.b64(o.id + "connection")
    case other ⇒ model.getHasher.b64(other.toString)
  }
}

trait InstantiateFromStringByApply[T <: Connection[_]] {
  def instantiate(from: String): Option[T]

  def apply(className: String, from: String): Option[T] =
    if (className == this.getClass.getSimpleName) instantiate(from)
    else None
}

trait InstantiateFromOctopusByApply[T <: Connection[_]] {
  def instantiate(from: Octopus): T

  def apply(className: String, from: Octopus): Option[T] =
    if (className == this.getClass.getSimpleName) Option(instantiate(from))
    else None
}