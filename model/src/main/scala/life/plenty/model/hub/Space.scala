package life.plenty.model.hub

import life.plenty.model.connection.Title
import life.plenty.model.hub.definition.Hub

//trait Space extends Octopus {
trait Space extends Hub {
  def getTitle = rx.get({ case Title(t) ⇒ t  })
  lazy val title = getInsertFeed.collect {case Title(t) ⇒ t}

  addToRequired(getTitle)

  //  override def idGenerator: String = super.idGenerator + (getTitle: String)
}




