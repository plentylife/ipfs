package life.plenty.model.modifiers

import life.plenty.model
import life.plenty.model.connection.MarkerEnum._
import life.plenty.model.connection._
import life.plenty.model.octopi.definition.{AtInstantiation, Hub}
import rx.{Ctx, Rx}

class InactiveFilter(override val withinOctopus: Hub) extends RxConnectionFilters[Hub] {

  println(s"InactiveFilter instantiated in ${withinOctopus.getClass.getSimpleName}")

//  private implicit val ctx: Ctx.Data = Ctx.Owner.safe()

  override def apply(what: Rx[Option[DataHub[_]]])(implicit ctx: Ctx.Owner): Rx[Option[DataHub[_]]] = {
    val filtered: Rx[Option[DataHub[_]]] = what.map { optCon: Option[DataHub[_]] ⇒
      optCon flatMap { con: DataHub[_] ⇒
        // checking if removed based on connection
        // the AtInstantiation is added for 2 reasons: not to trip an error of idGen and because those are required
        // (usually)
        if (!(con.isInstanceOf[Id] || con.tmpMarker == AtInstantiation)) {
          // is connection active? if not, remove
          if (con.isActive()) {
            // is the contents of the connection active? if not, remove
            filterOnValue(con)
          } else {
            model.console.trace(s"InactiveFilter filtered directly ${con} ${con.id} ")
            None
          }
        } else {
          optCon
        }
      }
    }
    model.console.trace(s"RemoveFilter $what -> ${filtered} in ${withinOctopus.getClass.getSimpleName}")
    filtered
  }

  private def filterOnValue(con: DataHub[_])(implicit ctx: Ctx.Data): Option[DataHub[_]] = {
    con.value match {
      case o: Hub ⇒ if (o.isActive()) {
        model.console.trace(s"InactiveFilter passed contents ${con} ${con.id} ")
        Option(con)
      } else {
        model.console.trace(s"InactiveFilter filtered contents ${con} ${con.id} ")
        None
      }
      case _ ⇒
        model.console.trace(s"InactiveFilter passed contents based on not being a hub ${con} ${con.id} ")
        Option(con)
    }
  }
}
