package life.plenty.model.modifiers

import life.plenty.model.connection.MarkerEnum._
import life.plenty.model.connection.{Connection, Marker}
import life.plenty.model.octopi.Octopus
import rx.{Ctx, Rx}

class RemovedFilter(override val withinOctopus: Octopus) extends RxConnectionFilters[Octopus] {

  override def apply(what: Rx[Option[Connection[_]]])(implicit ctx: Ctx.Owner): Rx[Option[Connection[_]]] = {
    println(s"filter rx $what ${what.now}")
    val filtered: Rx[Option[Connection[_]]] = what.map {
      _ flatMap { con ⇒
        con.value match {
          case o: Octopus ⇒
            val rc = o.rx.get({ case m@Marker(REMOVED) ⇒ m })
            if (rc().isEmpty) Option(con) else None
          case _ ⇒ None
        }
      }
    }
    println(s"filtered rx ${filtered}")
    filtered
  }
}
