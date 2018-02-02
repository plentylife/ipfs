package life.plenty.model.actions

import life.plenty.model.connection.Marker
import life.plenty.model.connection.MarkerEnum._
import life.plenty.model.octopi.{Module, Octopus}

class ActionRemove(override val withinOctopus: Octopus) extends Module[Octopus] {
  def remove() = {
    withinOctopus.addConnection(Marker(REMOVED))
  }
}
