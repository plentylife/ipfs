package life.plenty.ui.display.feed

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.hub._
import life.plenty.model.hub.definition.Hub
import life.plenty.model.hub.pseudo.VoteGroup
import life.plenty.ui
import life.plenty.ui.display.FullUserBadge
import life.plenty.ui.display.utils.CardNavigation
import life.plenty.ui.display.utils.Helpers._
import life.plenty.ui.model.{DisplayModule, SimpleDisplayModule}
import monix.reactive.Observable
import org.scalajs.dom.Node
import rx.{Ctx, Rx}
import scalaz.std.list._
import scalaz.std.option._
import scalaz.std.map._

trait FeedAnswerDisplayImpl {self: FeedDisplaySimple[Answer] ⇒
  override protected def action(hub: Answer) =
        hub match {
          case _: Proposal ⇒ "proposed"
          case _: Contribution ⇒ "contributed"
          case _ ⇒ "answered"
        }


  override protected def actionTarget(hub:Answer) = {
    hub.body
  }

  override protected val cssClass: String = "answer"
}

trait FeedQuestionDisplayImpl {self: FeedDisplaySimple[Question] ⇒
  override protected def action(hub: Question) = "asked"
  override protected def actionTarget(hub: Question): Observable[String] = {
    hub.title
  }

  override protected val cssClass: String = "question"
}

trait FeedSpaceDisplayImpl {self: FeedDisplaySimple[Space] ⇒
  override protected def action(hub: Space) = "created space"
  override protected def actionTarget(hub: Space): Observable[String] = {
    hub.title
  }

  override protected val cssClass: String = "space"
}

trait FeedTransactionDisplayImpl {self: FeedDisplay[Transaction] ⇒
  private val action = "gave"
  private val cssClass: String = "transaction"

  @dom
  override def html(hub: Transaction): Binding[Node] = {
//    println(s"DISPLAYING ${this}")

    val amount = new BindableProperty(hub.getAmountOrZeroRx)(a ⇒ a + ui.thanks)

    <div class={"feed " + cssClass} id={hub.id}>
      {SimpleDisplayModule.html(FullUserBadge, hub.from).bind} {actionHtml(action).bind}
      {SimpleDisplayModule.html(FullUserBadge, hub.to).bind}
      <span class="amount">{amount.dom.bind}</span>
    </div>
  }
}

trait FeedVoteGroupDisplayImpl {self: FeedDisplay[VoteGroup] ⇒

  @dom
  override def html(what: VoteGroup): Binding[Node] = {
    implicit val ctx = Ctx.Owner.Unsafe
    val uv = VoteGroup.countByUser(what.votes)
    val uvb = new ListBindable(uv map {_.toList})

    <div class="feed vote-group">
      <p class="vote-group-title">Proposal <span class="proposal-body">{what.answer.getBody.dom.bind}</span> received
        votes</p>
      <p class="vote-group-body">
        {for(uve <- uvb()) yield voteEntry(uve._1, uve._2).bind}
      </p>
    </div>
  }

  @dom
  private def voteEntry(u: User, votes: Int): Binding[Node] = <span>
    {FullUserBadge.html(u).bind} {actionHtml("voted").bind} {plusMinus(votes).bind}
  </span>
}