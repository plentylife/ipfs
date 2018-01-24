package life.plenty.model.connection

import life.plenty.model.connection.MarkerEnum.MarkerEnum

import scala.util.Try

case class Marker(m: MarkerEnum) extends Connection[MarkerEnum] {
  override def value: MarkerEnum = m

  override def id: String = value.toString.hashCode.toBinaryString
}

object Marker extends InstantiateFromStringByApply[Marker] {
  override def instantiate(from: String): Option[Marker] = Try(Marker(MarkerEnum.withName(from))).toOption
}
