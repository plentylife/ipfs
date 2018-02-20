package life.plenty.model.connection

import java.util.Date

import life.plenty.model
import life.plenty.model.octopi.definition.{AtInstantiation, Hub}
import life.plenty.model.utils.GraphUtils

trait DataHub[T] extends Hub {
  def value: T

  // should not be touched outside hubs
  private var order: Int = -1
  def setOrder(o: Int) = if (order == -1) order = o
  def getOrder = order

  override def id: String = idGivenValue(value) + this.getClass.getSimpleName + order

  protected def idGivenValue(v: T): String = {
    try {
      val bigId = v match {
        case o: Hub ⇒ model.getHasher.b64(o.id + "connection")
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