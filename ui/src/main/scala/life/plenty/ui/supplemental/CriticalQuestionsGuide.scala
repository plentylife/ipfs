package life.plenty.ui.supplemental

import com.thoughtworks.binding.Binding.Vars
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.RxOpt
import life.plenty.model.connection.Critical
import life.plenty.model.octopi.Question
import life.plenty.model.octopi.definition.Hub
import life.plenty.model.utils.GraphUtils
import life.plenty.ui.display.{CardQuestionDisplayBase, Modal}
import life.plenty.ui.display.utils.Helpers.ListBindable
import life.plenty.ui.model.UiContext
import org.scalajs.dom.{Event, Node}
import rx.{Ctx, Rx}
import rx.async._
import rx.async.Platform._

import scala.concurrent.duration._

object CriticalQuestionsGuide {
  private implicit val ctx = Ctx.Owner.safe()

  def apply(): Unit = {
    UiContext.startingSpaceRx.foreach(_ foreach {space ⇒
      val critical: ListBindable[Question] = space.rx.getAllRaw({
          case Critical(q: Question) ⇒ q
        }) flatMap {qs ⇒ filterCritical(qs) }

      Modal.giveContentAndOpen(html(critical()), hasCloseButton = false)
    })

  }

  /** check that the question isn't finalized, or that the user has already answered it */
  private def filterCritical(list: List[RxOpt[Question]]): Rx[List[Question]] = Rx {
    println(s"FILTERING CRITICAL $list")
    list flatMap {rx ⇒ rx() flatMap {q ⇒
      val hasCreated = GraphUtils.getAllCreatedByInSpace(q, UiContext.getUser) // shouldn't be null
      println(s"HAS CREATED IN $q $hasCreated")
      if (hasCreated().exists(_.getHolder != q)) Some(q) else None
    }
    }
  }

  @dom
  private def html(critical: Vars[Question]): Binding[Node] = {
    <div class="critical-question-list">
      <p>Please answer all of the following questions</p>
      {board(critical).bind}
    </div>
  }

  @dom
  private def board(critical: Vars[Question]): Binding[Node] = {
    <div class="board">
      {for (q <- critical) yield CardQuestionDisplayBase.html(q, List(), (e: Event) => Unit).bind}
    </div>
  }


}
