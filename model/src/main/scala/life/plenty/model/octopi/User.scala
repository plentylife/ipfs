package life.plenty.model.octopi

trait User extends Octopus {
  protected val _id: String

  override def id: String = _id
}

class BasicUser(override protected val _id: String) extends User {

}