package life.plenty.ui.display.feed

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.hub._
import life.plenty.model.hub.definition.Hub
import life.plenty.ui
import life.plenty.ui.display.FullUserBadge
import life.plenty.ui.display.utils.CardNavigation
import life.plenty.ui.display.utils.Helpers._
import life.plenty.ui.model.{DisplayModule, SimpleDisplayModule}
import org.scalajs.dom.Node
import rx.{Ctx, Rx}
import scalaz.std.list._
import scalaz.std.option._

trait FeedAnswerDisplayImpl {self: FeedDisplaySimple[Answer] ⇒
  override protected def action(hub: Answer)(implicit ctx: Ctx.Owner) = Rx {
        hub match {
          case _: Proposal ⇒ "proposed"
          case _: Contribution ⇒ "contributed"
          case _ ⇒ "answered"
        }
      }

  override protected def actionTarget(implicit hub:Answer, ctx: Ctx.Owner): Rx[String] = {
    hub.getBody
  }

  override protected val cssClass: String = "answer"
}

trait FeedQuestionDisplayImpl {self: FeedDisplaySimple[Question] ⇒
  override protected def action(hub: Question)(implicit ctx: Ctx.Owner) = Rx {"asked"}
  override protected def actionTarget(implicit hub:Question, ctx: Ctx.Owner): Rx[String] = {
    hub.getTitle
  }

  override protected val cssClass: String = "question"
}

trait FeedSpaceDisplayImpl {self: FeedDisplaySimple[Space] ⇒
  override protected def action(hub: Space)(implicit ctx: Ctx.Owner) = Rx {"created"}
  override protected def actionTarget(implicit hub:Space, ctx: Ctx.Owner): Rx[String] = {
    hub.getTitle
  }

  override protected val cssClass: String = "space"
}

trait FeedTransactionDisplayImpl {self: FeedDisplay[Transaction] ⇒
  private val action = "gave"
  private val cssClass: String = "transaction"

  @dom
  override def html(hub: Transaction): Binding[Node] = {
    val amount = new BindableProperty(hub.getAmountOrZero)(a ⇒ a + ui.thanks)

    <div class={"feed " + cssClass} id={hub.id}>
      {SimpleDisplayModule.html(FullUserBadge, hub.getFrom).bind} {actionHtml(action).bind}
      {SimpleDisplayModule.html(FullUserBadge, hub.getTo).bind}
      <span class="amount">{amount.dom.bind}</span>
    </div>
  }
}
