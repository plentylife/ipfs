package life.plenty.model.connection

import java.util.Date

import life.plenty.model
import life.plenty.model.octopi.definition.{AtInstantiation, Hub}
import life.plenty.model.utils.GraphUtils

trait DataHub[T] extends Hub {
  def value: T

  // should not be touched outside hubs
  private var holderId: String = ""
  def setHolder(hub: Hub): Unit = if (holderId.isEmpty) holderId = hub.id
  def getHolder: String = holderId

  override def id: String = holderPrefix + idGivenValue(value)

  private def holderPrefix = if (holderId.length >= 5) holderId.substring(0, 5) else holderId

  protected val idLength = 5
  protected def idGivenValue(v: T): String = {
    try {
      val bigId = v match {
        case o: Hub ⇒ model.getHasher.b64(o.id + "connection")
        case other ⇒ model.getHasher.b64(other.toString)
      }

      bigId.substring(0, idLength)
    } catch {
      case e: Throwable ⇒
        model.console.error(s"Error in connection id generator with value ${value}");
        model.console.error(e)
        e.printStackTrace();
        throw e
    }
  }

  def inst: DataHub[T] = {
    this.tmpMarker = AtInstantiation
    this
  }
}

trait InstantiateFromStringByApply[T <: DataHub[_]] {
  def instantiate(from: String): Option[T]

  def apply(className: String, from: String): Option[T] =
    if (className == this.getClass.getSimpleName) instantiate(from)
    else None
}

trait InstantiateFromOctopusByApply[T <: DataHub[_]] {
  // fixme. ideally should be optional
  def instantiate(from: Hub): T

  def apply(className: String, from: Hub): Option[T] = {
    //    println(s"applying on connection ${this.getClass.getSimpleName} == ${className}")
    if (className == this.getClass.getSimpleName) Option(instantiate(from))
    else None
  }

}