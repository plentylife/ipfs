package life.plenty.ui.display

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.actions.ActionUpDownVote
import life.plenty.model.octopi._
import life.plenty.ui.display.actions.{ChangeParent, EditSpace}
import life.plenty.ui.model.DisplayModel.DisplayModule
import life.plenty.ui.model.Helpers._
import life.plenty.ui.model.UiContext
import org.scalajs.dom.Event
import org.scalajs.dom.raw.Node
import rx.Rx
//import life.plenty.ui.model.DisplayModel.intToStr

class ProposalDisplay(override val withinOctopus: Proposal) extends DisplayModule[Proposal] {
  override def update(): Unit = {
    //    votes.value_=(withinOctopus.votes)
    //    body.value_=(withinOctopus._body)
  }

  private lazy val editor: BindableAction[EditSpace] = new BindableAction(withinOctopus.getTopModule({ case
    m: EditSpace ⇒ m
  }), this)

  @dom
  override protected def generateHtml(): Binding[Node] = {
    val disabled = findVoteModule.isEmpty
    <div class="card d-inline-flex mt-1 mr-1 flex-row answer">
      <div class="d-inline-flex flex-column controls">
        <button type="button" class="btn btn-primary btn-sm" disabled={disabled} onclick={upVote _}>Up vote</button>
        <button type="button" class="btn btn-primary btn-sm" disabled={disabled} onclick={downVote _}>Down vote</button>
        <span>
          {withinOctopus.votes.dom.bind}
          votes</span>
      </div>
      <div class="card-body">
        <h6 class="card-title">proposal</h6>
        <h6 class="card-subtitle mb-2 text-muted">by
          {Rx {
          withinOctopus.getCreator().map(_.getNameOrEmpty())
        }.dom.bind}
        </h6>
        <p class="card-text">
          {withinOctopus.getBody.dom.bind}
        </p>
      </div>

      <div class="card-controls-bottom d-inline-flex">
        {ChangeParent.displayActiveOnly(withinOctopus).bind}{editor.dom.bind}
      </div>
    </div>
  }

  private lazy val findVoteModule = withinOctopus.getTopModule({ case m: ActionUpDownVote ⇒ m })

  private def upVote(e: Event) = {
    findVoteModule.foreach(_.up(UiContext.getUser))
    //    votes.value_=(withinOctopus.votes)
  }

  private def downVote(e: Event) = {
    findVoteModule.foreach(_.down(UiContext.getUser))
    //    votes.value_=(withinOctopus.votes)
  }

}

