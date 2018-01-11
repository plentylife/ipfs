package life.plenty.model.connection

import life.plenty.model.User

case class Creator[String](user: String) extends Connection[String] {
  override val value: String = user
}

trait UserConnection extends Connection[User] {
  val user: User
  override val value: User = user
}

case class Contributor(override val user: User) extends UserConnection {
}

case class Member(override val user: User) extends UserConnection {
}
