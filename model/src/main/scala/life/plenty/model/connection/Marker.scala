package life.plenty.model.connection

import life.plenty.model.connection.MarkerEnum.MarkerEnum

import scala.util.Try

case class Marker(m: MarkerEnum) extends Connection[MarkerEnum] {
  override def value: MarkerEnum = m
}

object Marker extends InstantiateFromStringByApply[Marker] {
  override def instantiate(from: String): Option[Marker] = Try(Marker(MarkerEnum.withName(from))).toOption
}

case class Removed(idOfConnection: String) extends Connection[String] {
  override def value = idOfConnection
}

object Removed extends InstantiateFromStringByApply[Removed] {
  override def instantiate(from: String): Option[Removed] = Option(Removed(from))
}

