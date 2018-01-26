package life.plenty.model.connection

case class CreationTime(time: Long) extends Connection[Long] {
  override def value: Long = time
}
