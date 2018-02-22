package life.plenty.model

import scala.concurrent.ExecutionContext.Implicits.global
import life.plenty.model.connection._
import life.plenty.model.octopi.definition.Hub
import rx.{Ctx, Rx}

import scala.concurrent.Future

package object utils {

  /** unsafe */
  //  implicit def getRx[T](r: Rx[Option[T]]): T = r.now.get

  implicit class OptRxOctopus[T <: Hub](rx: Rx[Option[T]])(implicit ctx: Ctx.Owner) {
    console.trace(s"Util OptRxOcto ${rx.now}")
    def addConnection[T](c: DataHub[_]): Rx[Option[Future[Unit]]] =
      addConnection(c, () ⇒ Unit)

    def addConnection[T](c: DataHub[_], execOnSuccess: () ⇒ T)
    : Rx[Option[Future[T]]] = rx.map(_.map {
      o: Hub ⇒ o.addConnection(c) map (_ ⇒ execOnSuccess())
    })

    def forEach(f: T ⇒ Unit) = rx.foreach(o ⇒ o.foreach(f))
  }

  class Console(var active: Boolean, traceActive: Boolean = false, _prefix: String = "") {
    def println(s: ⇒ String) = if (active) Predef.println(prefix + s)

    def error(s: ⇒ String) = Predef.println(prefix + " ERROR " + s)

    def error(e: Throwable) = Predef.println(prefix + " ERROR " + e)

    def trace(s: ⇒ String) = if (traceActive) println(s)

    def prefix = if (_prefix.nonEmpty) _prefix + " : " else ""
  }
}
