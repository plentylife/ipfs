package life.plenty.ui.display

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.hub._
import life.plenty.model.hub.definition.Hub
import life.plenty.model.hub.pseudo.VoteGroup
import life.plenty.model.utils.{GraphEx, GraphExtractorsDEP, GraphUtils}
import life.plenty.ui.display.user.{FullUserBadge, UserDisplayDirectory}
import life.plenty.ui.display.utils.{FutureDom, FutureOptVar, FutureVar}
import life.plenty.ui.display.utils.Helpers.{BindableHtmlProperty, OptBindableHtmlProperty, OptBindableHub, OptBindableProperty}
import life.plenty.ui.model._
import org.scalajs.dom.Node
import rx.{Ctx, Rx}
import FutureDom._
import life.plenty.model.connection.Inactive
package object feed {
  sealed trait FeedDisplay[T] extends SimpleDisplayModule[T] {
    @dom
    protected def actionHtml(a: String): Binding[Node] = <span class="action-text">{a}</span>
  }

  sealed trait FeedDisplaySimple[T <: Hub] extends FeedDisplay[T] {

    protected def action(hub: T): String
    protected def actionTarget(implicit hub:T): FutureVar[String]
    protected val cssClass: String

    @dom
    private def actionTargetHtml(at: String): Binding[Node] = <span class="action-target-text">{at}</span>

    @dom
    override def html(hub: T): Binding[Node] = {
      val creator = new FutureOptVar(GraphEx.getCreator(hub))

      <div class={"feed " + cssClass} id={hub.id}>
        {dirDom[User](creator, UserDisplayDirectory).bind} {actionHtml(action(hub)).bind} {
          FutureDom.dom(actionTarget(hub), actionTargetHtml).bind
        }
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

  case object FeedDeletedDisplay extends FeedDisplaySimple[Inactive] with FeedDeletedDisplayImpl {
    override def fits(hub: Any): Boolean = hub.isInstanceOf[Inactive]
  }

  case object FeedTransactionDisplay extends FeedDisplay[Transaction] with FeedTransactionDisplayImpl {
    override def fits(hub: Any): Boolean = hub.isInstanceOf[Transaction]
  }

  case object FeedVoteGroupDisplay extends FeedDisplay[VoteGroup] with FeedVoteGroupDisplayImpl {
    override def fits(hub: Any): Boolean = hub.isInstanceOf[VoteGroup]
  }

  object FeedModuleDirectory extends SimpleDisplayModuleDirectory[Any] {
    override val directory:List[SimpleDisplayModule[_]] = List(
      FeedQuestionDisplay, FeedAnswerDisplay, FeedTransactionDisplay, FeedVoteGroupDisplay, FeedSpaceDisplay,
      FeedDeletedDisplay
    )
  }

}
