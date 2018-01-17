package life.plenty.model.connection

import life.plenty.model.octopi.User

case class Creator[String](user: String) extends Connection[String] {
  override def value: String = user
}

trait UserConnection extends Connection[User] {
  val user: User

  override def value: User = user
}

case class Contributor(override val user: User) extends UserConnection {
}

case class Member(override val user: User) extends UserConnection {
}
