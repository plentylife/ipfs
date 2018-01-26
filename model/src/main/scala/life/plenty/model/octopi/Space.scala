package life.plenty.model.octopi

import life.plenty.model.connection.Title
import life.plenty.model.utils._

//trait Space extends Octopus {
trait Space extends Octopus {
  def getTitle = rx.get({ case Title(t) ⇒ t })

  override def required = super.required + getTitle

  println(s"space required fields ${required}")

  override def idGenerator: String = super.idGenerator + (getTitle: String)
}




