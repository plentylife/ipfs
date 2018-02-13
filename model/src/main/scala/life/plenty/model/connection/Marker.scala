package life.plenty.model.connection

import life.plenty.model.connection.MarkerEnum.MarkerEnum

import scala.util.Try

case class Marker(m: MarkerEnum) extends DataHub[MarkerEnum] {
  override def value: MarkerEnum = m
}

object Marker extends InstantiateFromStringByApply[Marker] {
  override def instantiate(from: String): Option[Marker] = Try(Marker(MarkerEnum.withName(from))).toOption
}

