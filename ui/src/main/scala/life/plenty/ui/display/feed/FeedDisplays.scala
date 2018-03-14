package life.plenty.ui.display.feed

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi._
import life.plenty.model.octopi.definition.Hub
import life.plenty.ui
import life.plenty.ui.display.utils.CardNavigation
import life.plenty.ui.display.utils.Helpers._
import life.plenty.ui.model.DisplayModule
import org.scalajs.dom.Node
import rx.{Ctx, Rx}
import scalaz.std.list._
import scalaz.std.option._

//class FeedAnswerDisplay(override val hub: Answer) extends FeedDisplaySimple[Answer] {
//  override protected val action = Rx {
//    if (hub.isInstanceOf[Proposal]) "proposed"
//    else if (hub.isInstanceOf[Contribution]) "contributed" else "answered"
//  }
//  override protected val actionTarget: Rx[String] = hub.getBody
//  override protected val cssClass: String = "answer"
//}

trait FeedQuestionDisplayImpl {self: FeedDisplaySimple[Question] ⇒
  override protected def action(implicit ctx: Ctx.Owner) = Rx {"asked"}
  override protected def actionTarget(implicit hub:Question, ctx: Ctx.Owner): Rx[String] = {
    hub.getTitle
  }

  override protected val cssClass: String = "question"
}

//class FeedSpaceDisplay(override val hub: Space) extends FeedDisplaySimple[Space] {
//  override protected val action = Rx {"created"}
//  override protected val actionTarget: Rx[String] = hub.getTitle
//  override protected val cssClass: String = "space"
//}
//
//class FeedTransactionDisplay(override val hub: Transaction) extends FeedDisplay[Transaction] {
//  private val action = Rx {"gave"}
//  private val cssClass: String = "transaction"
//
//  @dom
//  override protected def generateHtml(): Binding[Node] = {
//    val os = overrides ::: cachedOverrides.bind.toList
//    val to = new OptBindableHub(hub.getTo, this, os)
//    val ab = new BindableHtmlProperty(action, actionHtml)
//    val amount = new BindableProperty(hub.getAmountOrZero)(a ⇒ a + ui.thanks)
//
//    <div class={"feed " + cssClass} id={hub.id}>
//      {new OptBindableHub(hub.getCreator, this, os).dom.bind} {ab.dom.bind} {to.dom.bind}
//      <span class="amount">{amount.dom.bind}</span>
//    </div>
//  }
//}
