package life.plenty.ui.display.feed

import life.plenty.model.utils.GraphEx._
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.hub._
import life.plenty.model.hub.definition.Hub
import life.plenty.model.hub.pseudo.VoteGroup
import life.plenty.ui
import life.plenty.ui.display.user.{FullUserBadge, UserDisplayDirectory}
import life.plenty.ui.display.utils._
import life.plenty.ui.display.utils.Helpers._
import life.plenty.ui.model.{DisplayModule, SimpleDisplayModule}
import org.scalajs.dom.Node
import rx.{Ctx, Rx}
import scalaz.std.list._
import scalaz.std.option._
import scalaz.std.map._
import FutureDom._
import life.plenty.model.utils.GraphEx

trait FeedAnswerDisplayImpl {self: FeedDisplaySimple[Answer] ⇒
  override protected def action(hub: Answer) =  {
        hub match {
          case _: Proposal ⇒ "proposed"
          case _: Contribution ⇒ "contributed"
          case _ ⇒ "answered"
        }
      }

  override protected def actionTarget(implicit hub:Answer): FutureVar[String] = {
    new FutureOptVar[String](getBody(hub))
  }

  override protected val cssClass: String = "answer"
}

trait FeedQuestionDisplayImpl {self: FeedDisplaySimple[Question] ⇒
  override protected def action(hub: Question) =  {"asked"}
  override protected def actionTarget(implicit hub:Question): FutureVar[String] = {
    new FutureOptVar[String](getTitle(hub))
  }

  override protected val cssClass: String = "question"
}

trait FeedSpaceDisplayImpl {self: FeedDisplaySimple[Space] ⇒
  override protected def action(hub: Space) =  {"created space"}
  override protected def actionTarget(implicit hub:Space): FutureVar[String] = {
    new FutureOptVar[String](getTitle(hub))
  }

  override protected val cssClass: String = "space"
}

trait FeedTransactionDisplayImpl {self: FeedDisplay[Transaction] ⇒
  private val action = "gave"
  private val cssClass: String = "transaction"

  @dom
  override def html(hub: Transaction): Binding[Node] = {
    println(s"DISPLAYING ${this}")

    val amount = new BindableProperty(hub.getAmountOrZeroRx)(a ⇒ a + ui.thanks)
    val toBadge = dirDom(GraphEx.getTo(hub), UserDisplayDirectory)
    val fromBadge = dirDom(GraphEx.getFrom(hub), UserDisplayDirectory)

    <div class={"feed " + cssClass} id={hub.id}>
      {fromBadge.bind} {actionHtml(action).bind}
      {toBadge.bind}
      <span class="amount">{amount.dom.bind}</span>
    </div>
  }
}

trait FeedVoteGroupDisplayImpl {self: FeedDisplay[VoteGroup] ⇒

  @dom
  override def html(what: VoteGroup): Binding[Node] = {
    implicit val ctx = Ctx.Owner.Unsafe
    val uv = VoteGroup.countByUser(what.votes)
    val uvb = new FutureList(uv)

    <div class="feed vote-group">
      <p class="vote-group-title">Proposal <span class="proposal-body">{what.answer.getBody.dom.bind}</span> received
        votes</p>
      <p class="vote-group-body">
        {for(uve <- uvb.v) yield voteEntry(uve._1, uve._2).bind}
      </p>
    </div>
  }

  @dom
  private def voteEntry(u: User, votes: Int): Binding[Node] = {
  val badge = UserDisplayDirectory.display(u).get
  <span>
    {badge.bind} {actionHtml("voted").bind} {plusMinus(votes).bind}
  </span>}
}