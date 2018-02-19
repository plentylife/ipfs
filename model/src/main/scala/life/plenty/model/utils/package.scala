package life.plenty.model

import life.plenty.model.connection._
import life.plenty.model.octopi.definition.Hub
import rx.{Ctx, Rx}

package object utils {

  /** unsafe */
  //  implicit def getRx[T](r: Rx[Option[T]]): T = r.now.get

  implicit class OptRxOctopus[T <: Hub](rx: Rx[Option[T]])(implicit ctx: Ctx.Owner) {
    console.trace(s"Util OptRxOcto ${rx.now}")
    def addConnection(c: DataHub[_]) = rx.foreach(_.foreach {
      o: Hub ⇒ o.addConnection(c)
    })
  }

  class Console(var active: Boolean, traceActive: Boolean = false, _prefix: String = "") {
    def println(s: ⇒ String) = if (active) Predef.println(prefix + s)

    def error(s: ⇒ String) = Predef.println(prefix + s)

    def error(e: Throwable) = Predef.println(prefix + e)

    def trace(s: ⇒ String) = if (traceActive) println(s)

    def prefix = if (_prefix.nonEmpty) _prefix + " : " else ""
  }

  object ConFinders {
    def getParent(o: Hub)(implicit ctx: Ctx.Owner) = o.rx.get({ case Parent(p: Hub) ⇒ p })

    def confirmedMarker(o: Hub)(implicit ctx: Ctx.Owner): Rx[Option[Marker]] =
      o.rx.get({ case c@Marker(m) if m == MarkerEnum.CONFIRMED ⇒ c })

    def markedConfirmed(o: Hub)(implicit ctx: Ctx.Owner): Rx[Boolean] =
      confirmedMarker(o).map(m ⇒ {m.nonEmpty})

    def getBody(h: Hub)(implicit ctx: Ctx.Owner): Rx[Option[String]] = h.rx.get({case Body(b) ⇒ b})

    // fixme use h.connections
    def active(o: Hub)(implicit ctx: Ctx.Owner): Rx[Boolean] = {
      val count: Rx[List[Int]] = o.connections.map {_ collect {
        case Marker(m) if m == MarkerEnum.INACTIVE ⇒ -1
        case Marker(m) if m == MarkerEnum.ACTIVE ⇒ 1
      }}
      count map {list: List[Int] ⇒
        val s = (0 :: list).sum
        s >= 0
      }
    }
  }
}
