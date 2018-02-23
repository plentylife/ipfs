package life.plenty.ui.filters

import life.plenty.model
import life.plenty.ui.console
import life.plenty.model.connection._
import life.plenty.model.modifiers.RxConnectionFilters
import life.plenty.model.octopi.{Transaction, User, VoteAllowance}
import life.plenty.model.octopi.definition.{AtInstantiation, Hub}
import life.plenty.model.utils.GraphUtils
import life.plenty.ui.model.UiContext
import rx.{Ctx, Rx}
class RootSpaceUserTransactionFilter(override val hub: User) extends RxConnectionFilters[User] {
//  private implicit val ctx = Ctx.Owner.safe()

  override def apply(what: Rx[Option[DataHub[_]]])(implicit ctx: Ctx.Owner): Rx[Option[DataHub[_]]] = Rx {
    val spOpt = UiContext.startingSpaceRx()
    // if starting space is not set, just pass
    spOpt.fold(what())(
      {sp ⇒
      val root = GraphUtils.getRootParentOrSelf(sp)
      val rootId = root().id

      what() flatMap { con: DataHub[_] ⇒ filter(rootId, con, ctx)}
    })
  }

  private def filter(rootId: String, con: DataHub[_], ctxOwner: Ctx.Owner)(implicit ctx: Ctx.Data): Option[DataHub[_]] = {
    console.trace(s"filtering on root $rootId connection $con")
    con match {
        // don't forget vote allowance
      case Child(t: Transaction) ⇒ filterTransaction(rootId, t, ctxOwner) map (_ ⇒ con)
      case Child(va: VoteAllowance) ⇒ va.onTransaction() flatMap {t ⇒
        filterTransaction(rootId, t, ctxOwner)} map {_ ⇒ con}
      case _ ⇒ Option(con)
    }
  }

  private def filterTransaction(rootId: String, t: Transaction, ctxOwner: Ctx.Owner)
                               (implicit ctx: Ctx.Data): Option[Unit] = {
    t.getOnContribution() flatMap {onContribution ⇒
      val crp = GraphUtils.getRootParentOrSelf(onContribution)(ctxOwner)
      console.trace(s"transaction parent root [$rootId] $crp ${crp.now.id} ${crp.now.id == rootId}")
      if (crp().id == rootId) Option(Unit) else None
    }
  }
}
