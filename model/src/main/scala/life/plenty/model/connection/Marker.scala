package life.plenty.model.connection

import life.plenty.model.connection.MarkerEnum.MarkerEnum

case class Marker(m: MarkerEnum) extends Connection[MarkerEnum] {
  override def value: MarkerEnum = m
}
