package life.plenty.model.connection

import life.plenty.model.octopi.{Octopus, User}

trait UserConnection extends Connection[User] {
  val user: User

  override def value: User = user
}

case class Creator(override val user: User) extends UserConnection {
}

object Creator extends InstantiateFromOctopusByApply[Creator] {
  override def instantiate(from: Octopus): Creator = {
    Creator(from.asInstanceOf[User])
  }
}

case class Contributor(override val user: User) extends UserConnection {
}

object Contributor extends InstantiateFromOctopusByApply[Contributor] {
  override def instantiate(from: Octopus): Contributor = {
    Contributor(from.asInstanceOf[User])
  }
}

case class Member(override val user: User) extends UserConnection {
}

object Member extends InstantiateFromOctopusByApply[Member] {
  override def instantiate(from: Octopus): Member = {
    Member(from.asInstanceOf[User])
  }
}

case class From(override val user: User) extends UserConnection

object From extends InstantiateFromOctopusByApply[From] {
  override def instantiate(from: Octopus): From = {
    From(from.asInstanceOf[User])
  }
}

case class To(override val user: User) extends UserConnection

object To extends InstantiateFromOctopusByApply[To] {
  override def instantiate(from: Octopus): To = {
    To(from.asInstanceOf[User])
  }
}