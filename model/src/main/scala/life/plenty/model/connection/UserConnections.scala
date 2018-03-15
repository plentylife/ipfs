package life.plenty.model.connection

import life.plenty.model.hub.User
import life.plenty.model.hub.definition.Hub

trait UserDataHub extends DataHub[User] {
  val user: User

  override def value: User = user
}

case class Creator(override val user: User) extends UserDataHub {
}

object Creator extends InstantiateFromOctopusByApply[Creator] {
  override def instantiate(from: Hub): Creator = {
    Creator(from.asInstanceOf[User])
  }
}

case class Contributor(override val user: User) extends UserDataHub {
}

object Contributor extends InstantiateFromOctopusByApply[Contributor] {
  override def instantiate(from: Hub): Contributor = {
    Contributor(from.asInstanceOf[User])
  }
}

case class Member(override val user: User) extends UserDataHub {
}

object Member extends InstantiateFromOctopusByApply[Member] {
  override def instantiate(from: Hub): Member = {
    Member(from.asInstanceOf[User])
  }
}

case class From(override val user: User) extends UserDataHub

object From extends InstantiateFromOctopusByApply[From] {
  override def instantiate(from: Hub): From = {
    From(from.asInstanceOf[User])
  }
}

case class To(override val user: User) extends UserDataHub

object To extends InstantiateFromOctopusByApply[To] {
  override def instantiate(from: Hub): To = {
    To(from.asInstanceOf[User])
  }
}