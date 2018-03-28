package life.plenty.model.actions

import java.util.Date

import life.plenty.model.connection.Inactive
import life.plenty.model.hub.definition.Hub
import life.plenty.model.utils.GraphEx._

import scala.concurrent.Future

object ActionDelete {

  def delete(what: Hub): Future[Unit] = {
    what.addConnection(Inactive(new Date().getTime))
  }

}
