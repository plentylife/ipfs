package life.plenty.model.modifiers

import life.plenty.model
import life.plenty.model.connection.MarkerEnum._
import life.plenty.model.connection._
import life.plenty.model.octopi.definition.Octopus
import rx.{Ctx, Rx}

class RemovedFilter(override val withinOctopus: Octopus) extends RxConnectionFilters[Octopus] {

  private implicit val ctx: Ctx.Owner = Ctx.Owner.safe()

  private lazy val removedConIds: Rx[List[String]] = withinOctopus.rx.Lazy.getAll({ case Removed(id: String) ⇒ id })

  override def apply(what: Rx[Option[Connection[_]]])(implicit ctx: Ctx.Owner): Rx[Option[Connection[_]]] = {
    val filtered: Rx[Option[Connection[_]]] = what.map { optCon: Option[Connection[_]] ⇒
      optCon flatMap { con: Connection[_] ⇒
        // checking if removed based on connection
        // the AtInstantiation is added for 2 reasons: not to trip an error of idGen and because those are required
        // (usually)
        if (!con.isInstanceOf[Id]) { // not loaded from db or instantiated
          model.console.trace(s"Removed connections list ${removedConIds.now} ${con} ${con.id} ${withinOctopus}")
          println(s"Removed connections list ${removedConIds.now} in ${con} ${con.id} ${withinOctopus}")
        }
        if (con.isInstanceOf[Id] || con.tmpMarker == AtInstantiation || !removedConIds().contains(con.id)) {
          val resCon: Option[Connection[_]] = con.value match {
            // checking if removed based on octopus
            case o: Octopus ⇒
              val rc = o.rx.Lazy.lazyGet({ case m@Marker(REMOVED) ⇒ m })
              val rcOpt = rc map { marker ⇒ if (marker.isEmpty) optCon else None }
              rcOpt()
            //            rc() map {_ ⇒ con}
            case _ ⇒ optCon
          }
          println(s"connection passed ${con} ${con.id} ")
          resCon
        } else {
          println(s"connection filtered ${con} ${con.id} ")
          None
        }
      }
    }
    println(s"RemoveFilter $what -> ${filtered}")
    //    model.console.trace(s"RemoveFilter $what -> ${filtered}")
    filtered
  }
}
