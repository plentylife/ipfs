package life.plenty.model.actions

import life.plenty.model.connection.Marker
import life.plenty.model.connection.MarkerEnum._
import life.plenty.model.hub.definition.{Module, Hub}

class ActionRemove(override val hub: Hub) extends Module[Hub] {
  def remove() = {
    hub.inactivate
  }
}
