package life.plenty.model.connection

case class CreationTime[Long](time: Long) extends Connection[Long] {
  override def value: Long = time
}
