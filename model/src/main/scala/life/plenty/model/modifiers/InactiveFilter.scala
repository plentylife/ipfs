package life.plenty.model.modifiers

import life.plenty.model
import life.plenty.model.connection.MarkerEnum._
import life.plenty.model.connection._
import life.plenty.model.octopi.definition.{AtInstantiation, Hub}
import rx.{Ctx, Rx}

class InactiveFilter(override val withinOctopus: Hub) extends RxConnectionFilters[Hub] {

  private implicit val ctx: Ctx.Owner = Ctx.Owner.safe()

  override def apply(what: Rx[Option[DataHub[_]]])(implicit ctx: Ctx.Owner): Rx[Option[DataHub[_]]] = {
    val filtered: Rx[Option[DataHub[_]]] = what.map { optCon: Option[DataHub[_]] ⇒
      optCon flatMap { con: DataHub[_] ⇒
        // checking if removed based on connection
        // the AtInstantiation is added for 2 reasons: not to trip an error of idGen and because those are required
        // (usually)
        if (!(con.isInstanceOf[Id] || con.tmpMarker == AtInstantiation)) {
          val resCon: Option[DataHub[_]] = con.value match {
            case o: Hub ⇒ if (o.isActive()) optCon else None
            case _ ⇒ optCon
          }
//          println(s"connection passed ${con} ${con.id} ")
          resCon
        } else {
          optCon
        }
      }
    }
    //    model.console.trace(s"RemoveFilter $what -> ${filtered}")
    filtered
  }
}
