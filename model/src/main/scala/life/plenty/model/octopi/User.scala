package life.plenty.model.octopi

import life.plenty.model.connection.Name
import rx.Rx

trait User extends Octopus {
  addToRequired(getRxId)
  addToRequired(getName)

  //  override def idGenerator: String = {
  //    throw new NotImplementedError(s"this method not supposed to be used for users. Connections ${_connections.now}")
  //  }

  def getName: Rx[Option[String]] = rx.get({ case Name(n: String) ⇒ n })

  override def equals(o: Any): Boolean = o match {
    case that: User => that.id.equalsIgnoreCase(this.id)
    case _ => false
  }

  override def hashCode: Int = id.hashCode
}

class BasicUser() extends User {

}