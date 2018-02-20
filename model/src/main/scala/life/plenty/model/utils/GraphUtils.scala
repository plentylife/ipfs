package life.plenty.model.utils

import life.plenty.model.connection._
import life.plenty.model.octopi.definition.Hub
import rx.{Ctx, Rx}

object GraphUtils {
  def getParent(o: Hub)(implicit ctx: Ctx.Owner) = o.rx.get({ case Parent(p: Hub) ⇒ p })

  def confirmedMarker(o: Hub)(implicit ctx: Ctx.Owner): Rx[Option[Marker]] =
    o.rx.get({ case c@Marker(m) if m == MarkerEnum.CONFIRMED ⇒ c })

  def markedConfirmed(o: Hub)(implicit ctx: Ctx.Owner): Rx[Boolean] =
    confirmedMarker(o).map(m ⇒ {m.nonEmpty})

  def getBody(h: Hub)(implicit ctx: Ctx.Owner): Rx[Option[String]] = h.rx.get({case Body(b) ⇒ b})

  // fixme use h.connections
  def isActive(o: Hub)(implicit ctx: Ctx.Owner): Rx[Boolean] = {
    val count: Rx[List[Int]] = o.connections.map {_ collect {
      case Inactive(_) ⇒ -1
      case Active(_) ⇒ 1
    }}
    count map {list: List[Int] ⇒
      val s = (0 :: list).sum
      s >= 0
    }
  }

  def findModuleUpParentTree[T](in: Hub, matchBy: PartialFunction[DataHub[_], T]): Option[T] = {
    {
      //      println(s"graph utils", in)
      val within = in.sc.ex(matchBy)
      //                  println("graph utils", within, in, in.connections)
      within orElse {
        in.sc.ex({ case Parent(p: Hub) ⇒ p }) flatMap {
          p ⇒
            if (p == in) {
              println("Error in findModule of ActionAddMember: same parent")
              None
            } else {
              findModuleUpParentTree(p, matchBy)
            }
        }
      }
    }
  }
}