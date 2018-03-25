package life.plenty.model.interfaces

import life.plenty.model.hub.definition.Hub

/** Used to load hubs and their connections */
trait ReaderSpec {
  def loadConnections(hub: Hub): Unit
}

object ReaderSpec {
  var interface: ReaderSpec = _
}


