package life.plenty.model.connection

case class Creator[String](user: String) extends Connection[String] {
  override val value: String = user
}
