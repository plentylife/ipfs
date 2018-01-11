package life.plenty.model

trait User extends Octopus {
  protected val _id: String

  override def id: String = _id
}

case class BasicUser(override protected val _id: String) extends User {

}