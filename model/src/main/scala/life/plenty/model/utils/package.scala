package life.plenty.model

import life.plenty.model.connection.{Connection, Marker, MarkerEnum}
import life.plenty.model.octopi.definition.Octopus
import rx.{Ctx, Rx}

package object utils {

  /** unsafe */
  //  implicit def getRx[T](r: Rx[Option[T]]): T = r.now.get

  implicit class OptRxOctopus[T <: Octopus](rx: Rx[Option[T]])(implicit ctx: Ctx.Owner) {
    console.trace(s"Util OptRxOcto ${rx.now}")
    def addConnection(c: Connection[_]) = rx.foreach(_.foreach {
      o: Octopus ⇒ o.addConnection(c)
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
    def markedConfirmed(o: Octopus)(implicit ctx: Ctx.Owner): Rx[Boolean] =
      o.rx.get({ case Marker(m) if m == MarkerEnum.CONFIRMED ⇒ m }).map(m ⇒ {
        println(s"rx confirmed changing ${m}")
        m.nonEmpty
      })
  }
}
