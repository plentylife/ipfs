package life.plenty.model.octopi

trait User extends Octopus {
  protected val _id: String

  override def id: String = _id

  override def equals(o: Any): Boolean = o match {
    case that: User => that.id.equalsIgnoreCase(this.id)
    case _ => false
  }

  override def hashCode: Int = id.hashCode
}

class BasicUser(override protected val _id: String) extends User {

}