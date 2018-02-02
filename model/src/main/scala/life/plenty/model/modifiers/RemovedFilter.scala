package life.plenty.model.modifiers

import life.plenty.model
import life.plenty.model.connection.MarkerEnum._
import life.plenty.model.connection.{Connection, Marker}
import life.plenty.model.octopi.Octopus
import rx.{Ctx, Rx}

class RemovedFilter(override val withinOctopus: Octopus) extends RxConnectionFilters[Octopus] {

  override def apply(what: Rx[Option[Connection[_]]])(implicit ctx: Ctx.Owner): Rx[Option[Connection[_]]] = {
    val filtered: Rx[Option[Connection[_]]] = what.map { optCon: Option[Connection[_]] ⇒
      optCon flatMap { con: Connection[_] ⇒
        val resCon: Option[Connection[_]] = con.value match {
          case o: Octopus ⇒
            val rc = o.rx.get({ case m@Marker(REMOVED) ⇒ m })
            val rcOpt = rc map { marker ⇒ if (marker.isEmpty) optCon else None }
            rcOpt()
          //            rc() map {_ ⇒ con}
          case _ ⇒ optCon
        }
        resCon
      }
    }
    model.console.trace(s"RemoveFilter $what -> ${filtered}")
    filtered
  }
}
