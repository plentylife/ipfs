package life.plenty.model.octopi

import life.plenty.model.connection.Title
import life.plenty.model.octopi.definition.Hub

//trait Space extends Octopus {
trait Space extends Hub {
  def getTitle = rx.get({ case Title(t) ⇒ t })

  addToRequired(getTitle)

  //  override def idGenerator: String = super.idGenerator + (getTitle: String)
}




