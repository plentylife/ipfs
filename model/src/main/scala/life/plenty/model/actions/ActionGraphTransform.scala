package life.plenty.model.actions

import life.plenty.model.connection.Connection
import life.plenty.model.{Module, Octopus}

trait ActionGraphTransform extends Module[Octopus] {
  def onConnectionAdd(connection: Connection[_]): Either[Exception, Unit]

  def onConnectionRemove(connection: Connection[_]): Either[Exception, Unit]
}
