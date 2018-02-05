package life.plenty.model.connection

import life.plenty.model
import life.plenty.model.octopi.Octopus

trait TmpMarker

object NoMarker extends TmpMarker

object AtInstantiation extends TmpMarker

trait Connection[T] {
  def value: T

  private var active: Boolean = true

  def isActive = active

  def activate = active = true

  def deactivate = active = false

  var tmpMarker: TmpMarker = NoMarker

  def id: String = idGivenValue(value) + this.getClass.getSimpleName

  protected def idGivenValue(v: T): String = {
    try {
      val bigId = v match {
        case o: Octopus ⇒ model.getHasher.b64(o.id + "connection")
        case other ⇒ model.getHasher.b64(other.toString)
      }

      bigId
    } catch {
      case e: Throwable ⇒
        model.console.error(s"Error in connection id generator with value ${value}");
        e.printStackTrace();
        throw e
    }
  }

  def inst: Connection[T] = {
    this.tmpMarker = AtInstantiation
    this
  }
}

trait InstantiateFromStringByApply[T <: Connection[_]] {
  def instantiate(from: String): Option[T]

  def apply(className: String, from: String): Option[T] =
    if (className == this.getClass.getSimpleName) instantiate(from)
    else None
}

trait InstantiateFromOctopusByApply[T <: Connection[_]] {
  // fixme. ideally should be optional
  def instantiate(from: Octopus): T

  def apply(className: String, from: Octopus): Option[T] = {
    //    println(s"applying on connection ${this.getClass.getSimpleName} == ${className}")
    if (className == this.getClass.getSimpleName) Option(instantiate(from))
    else None
  }

}