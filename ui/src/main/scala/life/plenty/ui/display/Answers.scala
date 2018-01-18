package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.actions.ActionUpDownVote
import life.plenty.model.connection.Child
import life.plenty.model.octopi._
import life.plenty.ui
import life.plenty.ui.Context
import life.plenty.ui.model.DisplayModel
import life.plenty.ui.model.DisplayModel.DisplayModule
import org.scalajs.dom.Event
import org.scalajs.dom.raw.Node

class BasicAnswerDisplay(override val withinOctopus: BasicAnswer) extends DisplayModule[BasicAnswer] {
  protected val body = Var[String](withinOctopus.body)
  protected val votes = Var[Int](calculateVotes)

  override def update(): Unit = body.value_=(withinOctopus.body)

  @dom
  override protected def generateHtml(overrides: List[DisplayModel.ModuleOverride]): Binding[Node] = {
    val disabled = findVoteModule.isEmpty
    <div class="card d-inline-flex mt-1 flex-row">
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
          {body.bind}
        </p>
      </div>
    </div>
  }

  private def findVoteModule = withinOctopus.getTopModule({ case m: ActionUpDownVote ⇒ m })

  private def upVote(e: Event) = {
    findVoteModule.foreach(_.up(Context.getUser))
    votes.value_=(calculateVotes)
  }

  private def calculateVotes = {
    val votes = 0 :: withinOctopus.connections.collect({ case Child(v: Vote) ⇒ v.sizeAndDirection })
    votes.sum
  }

  private def downVote(e: Event) = {
    findVoteModule.foreach(_.down(Context.getUser))
    votes.value_=(calculateVotes)
  }

}

class ContributionDisplay(override val withinOctopus: Contribution) extends DisplayModule[Contribution] {
  protected val body = Var[String](withinOctopus.body)

  override def update(): Unit = {
    body.value_=(withinOctopus.body)
  }

  @dom
  override def generateHtml(overrides: List[DisplayModel.ModuleOverride]): Binding[Node] = {
    <div class="card d-inline-flex mt-1 flex-row">
      <div class="d-inline-flex flex-column">
        <button type="button" class="btn btn-primary">Tip</button>
        <span>x
          {ui.thanks}
        </span>
      </div>
      <div class="card-body">
        <h6 class="card-title">contribution</h6>
        <h6 class="card-subtitle mb-2 text-muted">by sarah</h6>
        <p class="card-text">
          {body.bind}
        </p>
      </div>
    </div>
  }
}