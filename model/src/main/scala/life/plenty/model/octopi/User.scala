package life.plenty.model.octopi

trait User extends Octopus {
  override def required = super.required + getRxId

  override def idGenerator: String = throw new NotImplementedError("this method not supposed to be used for users")

  override def equals(o: Any): Boolean = o match {
    case that: User => that.id.equalsIgnoreCase(this.id)
    case _ => false
  }

  override def hashCode: Int = id.hashCode
}

class BasicUser() extends User {

}