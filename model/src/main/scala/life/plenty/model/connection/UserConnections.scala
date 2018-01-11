package life.plenty.model.connection

case class Creator[String](user: String) extends Connection[String] {
  override val value: String = user
}

case class Contributor[User](user: User) extends Connection[User] {
  override val value: User = user
}
