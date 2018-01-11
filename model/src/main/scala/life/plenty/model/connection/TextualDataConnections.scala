package life.plenty.model.connection

case class Title(title: String) extends Connection[String] {
  override val value: String = title
}

case class Body(body: String) extends Connection[String] {
  override val value: String = body
}
