package life.plenty.ui.filters

import life.plenty.model
import life.plenty.model.connection._
import life.plenty.model.modifiers.RxConnectionFilters
import life.plenty.model.octopi.{Transaction, User}
import life.plenty.model.octopi.definition.{AtInstantiation, Hub}
import life.plenty.model.utils.GraphUtils
import life.plenty.ui.model.UiContext
import rx.{Ctx, Rx}

class RootSpaceUserTransactionFilter(override val withinOctopus: User) extends RxConnectionFilters[User] {
//  private implicit val ctx = Ctx.Owner.safe()

  override def apply(what: Rx[Option[DataHub[_]]])(implicit ctx: Ctx.Owner): Rx[Option[DataHub[_]]] = Rx {
    UiContext.startingSpaceRx() flatMap {sp ⇒
      val root = GraphUtils.getRootParentOrSelf(sp)
      val rootId = root().id

      what() flatMap { con: DataHub[_] ⇒ filter(rootId, con, ctx)}
    }
  }

  private def filter(rootId: String, con: DataHub[_], ctxOwner: Ctx.Owner)(implicit ctx: Ctx.Data): Option[DataHub[_]] = {
    con match {
      case Child(t: Transaction) ⇒
        t.getOnContribution() flatMap {onContribution ⇒
          val crp = GraphUtils.getRootParentOrSelf(onContribution)(ctxOwner)
          if (crp().id == rootId) Option(con) else None
        }
      case _ ⇒ Option(con)
    }
  }
}
