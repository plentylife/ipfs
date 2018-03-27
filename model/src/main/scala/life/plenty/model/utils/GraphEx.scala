package life.plenty.model.utils

import life.plenty.model.connection._
import life.plenty.model.hub.{Members, User}
import life.plenty.model.hub.definition.Hub
import rx.{Ctx, Rx}

import scala.concurrent.Future

object GraphEx {
  def getCreationTime(h: Hub): Future[Option[Long]] = h.get({ case CreationTime(b) ⇒ b })
  def getCreator(h: Hub) = h.get({ case Creator(t) ⇒ t })

  def getBody(h: Hub): Future[Option[String]] = h.get({ case Body(b) ⇒ b })
  def getName(h: Hub): Future[Option[String]] = h.get({ case Name(b) ⇒ b })
  def getEmail(h: Hub): Future[Option[String]] = h.get({ case Email(b) ⇒ b })
  def getTitle(h: Hub): Future[Option[String]] = h.get({ case Title(b) ⇒ b })

}
