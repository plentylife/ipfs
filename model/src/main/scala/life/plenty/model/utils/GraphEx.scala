package life.plenty.model.utils

import life.plenty.model.connection.{CreationTime, Creator}
import life.plenty.model.hub.definition.Hub

import scala.concurrent.Future

object GraphEx {
  def getCreationTime(h: Hub): Future[Option[Long]] = h.get({ case CreationTime(b) ⇒ b })
  def getCreator(h: Hub) = h.get({ case Creator(t) ⇒ t })

}
