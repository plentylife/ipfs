package life.plenty.model

import life.plenty.model.connection.Connection
import life.plenty.model.octopi.Octopus
import rx.{Ctx, Rx}

package object utils {

  /** unsafe */
  implicit def getRx[T](r: Rx[Option[T]]): T = r.now.get

  implicit class OptRxOctopus[T <: Octopus](rx: Rx[Option[T]])(implicit ctx: Ctx.Owner) {
    console.println(s"Util OptRxOcto ${rx.now}")
    def addConnection(c: Connection[_]) = rx.foreach(_.foreach {
      o: Octopus ⇒ o.addConnection(c)
    })
  }

  class Console(var active: Boolean, traceActive: Boolean = false) {
    def println(s: String) = if (active) Predef.println(s)

    def error(s: String) = Predef.println(s)

    def error(e: Throwable) = Predef.println(e)

    def trace(s: String) = if (traceActive) println(s)
  }
}
