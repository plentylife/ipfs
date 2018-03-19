package life.plenty.ui.display

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.hub._
import life.plenty.model.hub.definition.Hub
import life.plenty.model.hub.pseudo.VoteGroup
import life.plenty.model.utils.GraphUtils
import life.plenty.model.utils.DeprecatedGraphExtractors
import life.plenty.ui.display.utils.DomValStream
import life.plenty.ui.display.utils.Helpers.{BindableHtmlProperty, OptBindableHtmlProperty, OptBindableHub, OptBindableProperty}
import life.plenty.ui.model._
import monix.reactive.Observable
import org.scalajs.dom.Node
import rx.{Ctx, Rx}

package object feed {
  sealed trait FeedDisplay[T] extends SimpleDisplayModule[T] {
    @dom
    protected def actionHtml(a: String): Binding[Node] = <span class="action-text">{a}</span>
  }

  sealed trait FeedDisplaySimple[T <: Hub] extends FeedDisplay[T] {

    protected def action(hub: T): String
    protected def actionTarget(hub: T): Observable[String]
    protected val cssClass: String

    @dom
    private def actionTargetHtml(at: String): Binding[Node] = <p class="action-target-text">{at}</p>

    @dom
    override def html(hub: T): Binding[Node] = {
//      println(s"DISPLAYING ${this}")

      implicit val c = hub.ctx
      val parent = DeprecatedGraphExtractors.getParent(hub)

      <div class={"feed " + cssClass} id={hub.id}>
        {SimpleDisplayModule.html(FullUserBadge, hub.creator).bind} {actionHtml(action(hub)).bind}
          <p class="action-target-text">{new DomValStream(actionTarget(hub)).dom.bind}</p>
      </div>
    }
  }

  case object FeedQuestionDisplay extends FeedDisplaySimple[Question] with FeedQuestionDisplayImpl {
    override def fits(hub: Any): Boolean = hub.isInstanceOf[Question]
  }

  case object FeedAnswerDisplay extends FeedDisplaySimple[Answer] with FeedAnswerDisplayImpl {
    override def fits(hub: Any): Boolean = hub.isInstanceOf[Answer]
  }

  case object FeedSpaceDisplay extends FeedDisplaySimple[Space] with FeedSpaceDisplayImpl {
    override def fits(hub: Any): Boolean = hub.isInstanceOf[Space]
  }

  case object FeedTransactionDisplay extends FeedDisplay[Transaction] with FeedTransactionDisplayImpl {
    override def fits(hub: Any): Boolean = hub.isInstanceOf[Transaction]
  }

  case object FeedVoteGroupDisplay extends FeedDisplay[VoteGroup] with FeedVoteGroupDisplayImpl {
    override def fits(hub: Any): Boolean = hub.isInstanceOf[VoteGroup]
  }

  object FeedModuleDirectory extends SimpleDisplayModuleDirectory[FeedDisplay[_]] {
    override val directory: List[FeedDisplay[_]] = List(
      FeedQuestionDisplay, FeedAnswerDisplay, FeedTransactionDisplay, FeedVoteGroupDisplay, FeedSpaceDisplay
    )
  }

}
