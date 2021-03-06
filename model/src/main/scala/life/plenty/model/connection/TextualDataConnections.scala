package life.plenty.model.connection

case class Title(title: String) extends Connection[String] {
  override def value: String = title
}

case class Body(body: String) extends Connection[String] {
  override def value: String = body
}

