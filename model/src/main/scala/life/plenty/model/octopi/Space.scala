package life.plenty.model.octopi

import life.plenty.model.connection.Title
import life.plenty.model.octopi.definition.Octopus

//trait Space extends Octopus {
trait Space extends Octopus {
  def getTitle = rx.get({ case Title(t) ⇒ t })

  addToRequired(getTitle)

  //  override def idGenerator: String = super.idGenerator + (getTitle: String)
}




