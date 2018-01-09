package life.plenty.model.connection

case class CreationTime[Long](time: Long) extends Connection[Long] {
  override val value: Long = time
}
