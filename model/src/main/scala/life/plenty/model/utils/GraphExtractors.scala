package life.plenty.model.utils

import life.plenty.model.connection._
import scala.concurrent.ExecutionContext.Implicits.global
import life.plenty.model.hub.definition.Hub
import life.plenty.model.hub.{Contribution, Members, Space, User}
import life.plenty.model.utils.GraphUtils.collectDownTreeRx
import rx.{Ctx, Rx}

import scala.concurrent.Future
import scala.language.postfixOps

object GraphExtractors {
  def getParent(o: Hub)(implicit ctx: Ctx.Owner) = o.rx.get({ case Parent(p: Hub) ⇒ p })

  private def getRootParentConnection(o: Hub)(implicit ctx: Ctx.Owner) = o.rx.get({ case c@RootParent(_) ⇒ c })

  def getRootParent(o: Hub)(implicit ctx: Ctx.Owner) = getRootParentConnection(o).map(_.map(_.value))

  def getRootParentOrSelf(o: Hub)(implicit ctx: Ctx.Owner): Rx.Dynamic[Hub] =
    getRootParentConnection(o).map(_.map(_.value).getOrElse(o))

  def confirmedMarker(o: Hub)(implicit ctx: Ctx.Owner): Rx[Option[Marker]] =
    o.rx.get({ case c@Marker(m) if m == MarkerEnum.CONFIRMED ⇒ c })

  def markedConfirmed(o: Hub)(implicit ctx: Ctx.Owner): Rx[Boolean] =
    confirmedMarker(o).map(m ⇒ {m.nonEmpty})

  def markedContributing(o: Hub)(implicit ctx: Ctx.Owner): Rx[Boolean] =
    o.rx.get({ case c@Marker(m) if m == MarkerEnum.CONTRIBUTING_QUESTION ⇒ c }).map(m ⇒ {m.nonEmpty})

  def getCritical(h: Hub)(implicit ctx: Ctx.Owner): Rx[List[Critical[Hub]]] = h.rx.getAll({
    case c : Critical[Hub] ⇒ c
  })

  def getBody(h: Hub)(implicit ctx: Ctx.Owner): Rx[Option[String]] = h.rx.get({ case Body(b) ⇒ b })
  def getName(h: Hub)(implicit ctx: Ctx.Owner): Rx[Option[String]] = h.rx.get({ case Name(b) ⇒ b })
  def getMemberships(u: User)(implicit ctx: Ctx.Owner): Future[Rx[List[Members]]] =
    u.loadCompleted map {_ ⇒ u.rx.getAll({ case Parent(m: Members) ⇒ m })}

  def getTitle(h: Hub)(implicit ctx: Ctx.Owner): Rx[Option[String]] = h.rx.get({ case Title(b) ⇒ b })
  def getCreationTime(h: Hub)(implicit ctx: Ctx.Owner): Rx[Option[Long]] = h.rx.get({ case CreationTime(b) ⇒ b })

  def isActive(o: Hub)(implicit ctx: Ctx.Owner): Rx[Boolean] = {
    val count: Rx[List[Int]] = o.connections.map {
      _ collect {
        case Inactive(_) ⇒ -1
        case Active(_) ⇒ 1
      }
    }
    count map { list: List[Int] ⇒
      val s = (0 :: list).sum
      s >= 0
    }
  }

  def getAllContributionsInSpace(space: Space)(implicit ctx: Ctx.Owner): Rx[List[Contribution]] = {
    collectDownTreeRx[Contribution](space, matchBy = {case Child(c: Contribution) ⇒ c},
      allowedPath = {case Child(h: Hub) ⇒ h}, 1000)
  }

  def getAllChildrenInSpace(space: Space)(implicit ctx: Ctx.Owner): Rx[List[Hub]] = {
    collectDownTreeRx[Hub](space, matchBy = {case Child(h: Hub) ⇒ h},
      allowedPath = {case Child(h: Hub) ⇒ h}, 1000)
  }

  def getAllCreatedByInSpace(space: Space, user: User)(implicit ctx: Ctx.Owner): Rx[List[Creator]] = {
    collectDownTreeRx[Creator](space, matchBy = {case c @ Creator(u) if u == user ⇒ c},
      allowedPath = {case Child(h: Hub) ⇒ h}, 1000)
  }
}