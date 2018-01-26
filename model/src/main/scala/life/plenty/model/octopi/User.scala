package life.plenty.model.octopi

trait User extends Octopus {
  override def required = Set(getRxId)

  override def idGenerator: String = {
    throw new NotImplementedError(s"this method not supposed to be used for users. Connections ${_connections.now}")
  }

  override def equals(o: Any): Boolean = o match {
    case that: User => that.id.equalsIgnoreCase(this.id)
    case _ => false
  }

  override def hashCode: Int = id.hashCode
}

class BasicUser() extends User {

}