package life.plenty.model.connection

import life.plenty.model.connection.MarkerEnum.MarkerEnum

import scala.util.Try

case class Marker(m: MarkerEnum) extends DataHub[MarkerEnum] {
  override def value: MarkerEnum = m
}

object Marker extends InstantiateFromStringByApply[Marker] {
  override def instantiate(from: String): Option[Marker] = Try(Marker(MarkerEnum.withName(from))).toOption
}

case class Inactive(time: Long) extends DataHub[Long] {
  override def value: Long = time
}

object Inactive extends InstantiateFromStringByApply[Inactive] {
  override def instantiate(from: String): Option[Inactive] = ConnectionsUtils.strToLong(from, Inactive(_))
}

case class Active(time: Long) extends DataHub[Long] {
  override def value: Long = time
}

object Active extends InstantiateFromStringByApply[Active] {
  override def instantiate(from: String): Option[Active] = ConnectionsUtils.strToLong(from, Active(_))
}

