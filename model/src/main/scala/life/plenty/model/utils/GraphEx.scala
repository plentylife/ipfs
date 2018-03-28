package life.plenty.model.utils

import life.plenty.model.connection._
import life.plenty.model.hub.{Members, User}
import life.plenty.model.hub.definition.Hub
import rx.{Ctx, Rx}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object GraphEx {
  def getCreationTime(h: Hub): Future[Option[Long]] = h.get({ case CreationTime(b) ⇒ b })
  def getCreator(h: Hub) = h.get({ case Creator(t) ⇒ t })
  def getParent(h: Hub): Future[Option[Hub]] = h.get({ case Parent(b: Hub) ⇒ b })

  def getBody(h: Hub): Future[Option[String]] = h.get({ case Body(b) ⇒ b })
  def getName(h: Hub): Future[Option[String]] = h.get({ case Name(b) ⇒ b })
  def getEmail(h: Hub): Future[Option[String]] = h.get({ case Email(b) ⇒ b })
  def getTitle(h: Hub): Future[Option[String]] = h.get({ case Title(b) ⇒ b })
  def getTitleOrBody(hub: Hub): Future[Option[String]] = {
    getTitle(hub) flatMap {
      case optT @ Some(t) if t.nonEmpty ⇒ Future(optT)
      case _ ⇒ getBody(hub)
    }
  }

  def getLastLogin(h: Hub): Future[Option[Long]] = h.get({ case LastLogin(b) ⇒ b })

  def getTo(h: Hub): Future[Option[User]] = h.get({ case To(b) ⇒ b })
  def getTransactionFrom(h: Hub): Future[Option[User]] = getCreator(h)

}
