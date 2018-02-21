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

    def error(s: ⇒ String) = Predef.println(prefix + " ERROR " + s)

    def error(e: Throwable) = Predef.println(prefix + " ERROR " + e)

    def trace(s: ⇒ String) = if (traceActive) println(s)

    def prefix = if (_prefix.nonEmpty) _prefix + " : " else ""
  }
}
