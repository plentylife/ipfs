package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.actions.ActionUpDownVote
import life.plenty.model.octopi._
import life.plenty.ui.UiContext
import life.plenty.ui.model.DisplayModel
import life.plenty.ui.model.DisplayModel.DisplayModule
import life.plenty.ui.model.Helpers._
import org.scalajs.dom.Event
import org.scalajs.dom.raw.Node
import rx.Ctx

class BasicAnswerDisplay(override val withinOctopus: BasicAnswer) extends DisplayModule[BasicAnswer] {
  implicit val ctx: Ctx.Owner = Ctx.Owner.safe()

  //  protected val body = Var[String](withinOctopus._body)
  protected val votes = Var[Int](0)

  override def update(): Unit = {
    votes.value_=(withinOctopus.countVotes)
    //    body.value_=(withinOctopus._body)
  }

  @dom
  override protected def generateHtml(overrides: List[DisplayModel.ModuleOverride]): Binding[Node] = {
    val disabled = findVoteModule.isEmpty
    <div class="card d-inline-flex mt-1 mr-1 flex-row answer">
      <div class="d-inline-flex flex-column">
        <button type="button" class="btn btn-primary" disabled={disabled} onclick={upVote _}>Up vote</button>
        <button type="button" class="btn btn-primary" disabled={disabled} onclick={downVote _}>Down vote</button>
        <span>
          {votes.bind.toString}
          votes</span>
      </div>
      <div class="card-body">
        <h6 class="card-title">answer</h6>
        <h6 class="card-subtitle mb-2 text-muted">by john</h6>
        <p class="card-text">
          {withinOctopus.body.dom.bind}
        </p>
      </div>
    </div>
  }

  private def findVoteModule = withinOctopus.getTopModule({ case m: ActionUpDownVote ⇒ m })

  private def upVote(e: Event) = {
    findVoteModule.foreach(_.up(UiContext.getUser))
    votes.value_=(withinOctopus.countVotes)
  }

  private def downVote(e: Event) = {
    findVoteModule.foreach(_.down(UiContext.getUser))
    votes.value_=(withinOctopus.countVotes)
  }

}

