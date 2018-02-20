package life.plenty.model.connection

import java.util.Date

import life.plenty.model
import life.plenty.model.octopi.definition.{AtInstantiation, Hub}
import life.plenty.model.utils.GraphUtils

trait DataHub[T] extends Hub {
//  clearRequired() // fixme. eh, not that great

  def value: T

//  private lazy val

  override def id: String = idGivenValue(value) + this.getClass.getSimpleName
//    {
//    GraphUtils.getParent(this).now.flatMap(_.getRxId.now).getOrElse("noparent") : String
//  }

  protected def idGivenValue(v: T): String = {
    try {
      println(s"idGivenValue of `$v`")
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