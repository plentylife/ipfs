package life.plenty.model.actions

import life.plenty.model.connection.Marker
import life.plenty.model.connection.MarkerEnum._
import life.plenty.model.octopi.definition.{Module, Hub}

class ActionRemove(override val withinOctopus: Hub) extends Module[Hub] {
  def remove() = {
    withinOctopus.addConnection(Marker(REMOVED))
  }
}
