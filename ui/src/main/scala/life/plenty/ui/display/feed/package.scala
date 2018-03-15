package life.plenty.ui.display

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.hub._
import life.plenty.model.hub.definition.Hub
import life.plenty.model.utils.GraphUtils
import life.plenty.ui.display.utils.Helpers.{BindableHtmlProperty, OptBindableHtmlProperty, OptBindableHub, OptBindableProperty}
import life.plenty.ui.model._
import org.scalajs.dom.Node
import rx.{Ctx, Rx}

package object feed {
  sealed trait FeedDisplay[T <: Hub] extends SimpleDisplayModule[T] {
    @dom
    protected def actionHtml(a: String): Binding[Node] = <p class="action-text">{a}</p>
  }

  sealed trait FeedDisplaySimple[T <: Hub] extends FeedDisplay[T] {

    protected def action(hub: T)(implicit ctx: Ctx.Owner): Rx[String]
    protected def actionTarget(implicit hub:T, ctx: Ctx.Owner): Rx[String]
    protected val cssClass: String

    @dom
    private def actionTargetHtml(at: String): Binding[Node] = <p class="action-target-text">{at}</p>

    @dom
    override def html(hub: T): Binding[Node] = {
      implicit val c = hub.ctx
      val parent = GraphUtils.getParent(hub)
      val atb = new BindableHtmlProperty(actionTarget(hub, c), actionTargetHtml)
      val ab = new BindableHtmlProperty(action(hub), actionHtml)

      <div class={"feed " + cssClass} id={hub.id}>
        {SimpleDisplayModule.html(FullUserBadge, hub.getCreator).bind} {ab.dom.bind} {atb.dom.bind}
      </div>
    }
  }

  case object FeedQuestionDisplay extends FeedDisplaySimple[Question] with FeedQuestionDisplayImpl {
    override def fits(hub: Hub): Boolean = hub.isInstanceOf[Question]
  }

  case object FeedAnswerDisplay extends FeedDisplaySimple[Answer] with FeedAnswerDisplayImpl {
    override def fits(hub: Hub): Boolean = hub.isInstanceOf[Answer]
  }

  case object FeedSpaceDisplay extends FeedDisplaySimple[Space] with FeedSpaceDisplayImpl {
    override def fits(hub: Hub): Boolean = hub.isInstanceOf[Space]
  }

  case object FeedTransactionDisplay extends FeedDisplay[Transaction] with FeedTransactionDisplayImpl {
    override def fits(hub: Hub): Boolean = hub.isInstanceOf[Transaction]
  }

  object FeedModuleDirectory extends SimpleDisplayModuleDirectory[FeedDisplay[_]] {
    override val directory: List[FeedDisplay[_]] = List(
      FeedQuestionDisplay, FeedAnswerDisplay, FeedTransactionDisplay, FeedSpaceDisplay
    )
  }

}
