package life.plenty.model.utils

import scala.concurrent.ExecutionContext.Implicits.global
import life.plenty.model
import life.plenty.model.connection._
import life.plenty.model.hub.{Contribution, Members, Space, User}
import life.plenty.model.hub.definition.Hub
import rx.{Ctx, Rx, Var}

import scala.concurrent.Future
import scala.language.postfixOps

object GraphUtils {
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
  def getMemberships(u: User)(implicit ctx: Ctx.Owner): Rx[List[Members]] =
    u.rx.getAll({ case Parent(m: Members) ⇒ m })
  def getTitle(h: Hub)(implicit ctx: Ctx.Owner): Rx[Option[String]] = h.rx.get({ case Title(b) ⇒ b })

  // fixme use h.connections
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

  def findUpParentTree[T](in: Hub, matchBy: PartialFunction[DataHub[_], T])(implicit ctx: Ctx.Owner)
  : Rx[Option[T]] = Rx {

      //      println(s"graph utils", in)
      val within = in.rx.get(matchBy)
      //                  println("graph utils", within, in, in.connections)
      within() orElse {
        val p = in.rx.get({ case Parent(p: Hub) ⇒ p })
        p() flatMap {
          p ⇒
            if (p == in) {
              println("Error in findModule of ActionAddMember: same parent")
              None
            } else {
              val f = findUpParentTree(p, matchBy)
              f()
            }
        }
      }
  }

  def hasParentInChain(hub: Hub, parents: List[Hub])(implicit ctx: Ctx.Owner): Future[Boolean] = {
    if (parents contains hub) Future(true) else {
      hub.loadCompleted flatMap {_ ⇒
        hub.sc.ex({ case Parent(p: Hub) ⇒ p }) match {
            case Some(p) ⇒ hasParentInChain(p, parents)
            case None ⇒ Future(false)
          }
        }
      }
  }

  import rx.async._
  import rx.async.Platform._
  import scala.concurrent.duration._

  /** @param matchBy should be able to handle [[DataHub]] */
  @deprecated
  def collectDownTreeRx[T <: Hub](in: Hub, matchBy: PartialFunction[DataHub[_], T],
                                  allowedPath: PartialFunction[DataHub[_],Hub], debounceDuration: Int = 0)
                                 (implicit ctx: Ctx.Owner): Rx[List[T]] = Rx {
    val pathCons = in.rx.getAll(allowedPath).debounce(debounceDuration millis)
    val _hubs = in.rx.cons.debounce(debounceDuration millis)
    val hubs = _hubs map {list ⇒
      list collect matchBy
    }

    val nextHubs = pathCons() flatMap { h ⇒
      val r = collectDownTreeRx(h, matchBy, allowedPath)
      r()
    }

    hubs() ::: nextHubs
  }

  /** @param matchBy should be able to handle [[DataHub]] */
  def collectDownTree[T <: Hub](in: Hub, matchBy: PartialFunction[DataHub[_], T],
                                  allowedPath: PartialFunction[DataHub[_],Hub]): Future[List[T]] = {
    val pathCons = in.getAll(allowedPath)
    val hubs = in.getAll(matchBy)

    val nextHubs = pathCons flatMap { paths ⇒
      val hs = paths map {p ⇒ collectDownTree(p, matchBy, allowedPath)}
      Future.sequence(hs) map {_.flatten}
    }

    for (h ← hubs; nh ← nextHubs) yield h ::: nh
  }

}