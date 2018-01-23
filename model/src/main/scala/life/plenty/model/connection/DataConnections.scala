package life.plenty.model.connection

case class Title(title: String) extends Connection[String] {
  override def value: String = title
}

object Title extends InstantiateFromStringByApply[Title] {
  override def instantiate(from: String): Option[Title] = Option(Title(from))
}

case class Body(body: String) extends Connection[String] {
  override def value: String = body
}

case class Amount(amount: Int) extends Connection[Int] {
  override def value: Int = amount
}

case class Id(idValue: String) extends Connection[String] {
  override def value: String = idValue
}