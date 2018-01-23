package life.plenty.model.octopi

trait User extends Octopus {
  protected val userId: String

  override def idGenerator: String = userId

  override def equals(o: Any): Boolean = o match {
    case that: User => that.id.equalsIgnoreCase(this.id)
    case _ => false
  }

  override def hashCode: Int = id.hashCode
}

class BasicUser(override protected val userId: String) extends User {

}