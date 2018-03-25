package life.plenty.ui.supplemental

import com.thoughtworks.binding.Binding.Vars
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.RxOpt
import life.plenty.model.connection.Critical
import life.plenty.model.hub.Question
import life.plenty.model.hub.definition.Hub
import life.plenty.model.utils.GraphUtils
import life.plenty.model.utils.GraphExtractorsDEP
import life.plenty.ui
import life.plenty.ui.display.Modal
import life.plenty.ui.display.cards.CardQuestionDisplayBase
import life.plenty.ui.display.utils.Helpers.ListBindable
import life.plenty.ui.model.{Router, UiContext}
import org.scalajs.dom.{Event, Node}
import rx.{Ctx, Rx, Var ⇒ rxVar}
import rx.async._
import rx.async.Platform._

import scala.concurrent.duration._
import scala.scalajs.js

object CriticalQuestionsGuide {
  private implicit val ctx = Ctx.Owner.safe()

  def apply(): Unit = {
    UiContext.pointerRx.foreach(_ foreach { space ⇒
      val critical: ListBindable[Question] = space.rx.getAllRaw({
          case Critical(q: Question) ⇒ q
        }) flatMap {qs ⇒ filterCritical(qs) }

      var opened = false
      critical.rxv foreach {list ⇒
        if (list.nonEmpty && !opened) {
          js.timers.setTimeout(2000){
            if (opened) Modal.giveContentAndOpen(this, html(critical()), hasCloseButton = false)
          }
          opened = true
        } else if (list.isEmpty) {
          Modal.remove(this)
          opened = false
        }
      }

    })

  }

  /** check that the question isn't finalized, or that the user has already answered it */
  private def filterCritical(list: List[RxOpt[Question]]): Rx[List[Question]] = Rx {
    list flatMap {rx ⇒ rx() flatMap {q ⇒
      val r = filterSingleCritical(q)
      r()
    }}
  }

  private def filterSingleCritical(q: Question)(): Rx[Option[Question]] = {
    val hasCreated = GraphExtractorsDEP.getAllCreatedByInSpace(q, UiContext.getUser) // shouldn't be null
    val isFinalized = GraphExtractorsDEP.markedConfirmed(q)
    Rx {
      if (isFinalized() || hasCreated().exists(_.getHolder != q)) None else Some(q)
    }
  }

  private var waitingOn: rxVar[Option[Question]] = rxVar(None)

  private def onOpen(q: Question)(e: Event) = {
    Modal.remove(this)
    Router.navigateToHub(q)
  }

  @dom
  private def html(critical: Vars[Question]): Binding[Node] = {
    <div class="critical-question-list">
      <p>Please answer all of the following questions. Voting, giving {ui.thanks}hanks, asking sub-questions and
        creating sub-spaces also counts.</p>
      {board(critical).bind}
    </div>
  }

  @dom
  private def board(critical: Vars[Question]): Binding[Node] = {
    <div class="board">
      {for (q <- critical) yield CardQuestionDisplayBase.html(q, List(), onOpen(q)).bind}
    </div>
  }


}
