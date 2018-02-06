package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.actions.ActionUpDownVote
import life.plenty.model.octopi._
import life.plenty.ui.display.actions.{ChangeParent, EditSpace}
import life.plenty.ui.model.DisplayModel.DisplayModule
import life.plenty.ui.model.Helpers._
import life.plenty.ui.model.{DisplayModel, UiContext}
import org.scalajs.dom.Event
import org.scalajs.dom.raw.Node
import rx.Obs

import scalaz.std.list._
import scalaz.std.option._
//import life.plenty.ui.model.DisplayModel.intToStr

class ProposalDisplay(override val withinOctopus: Proposal) extends DisplayModule[Proposal] {
  private var obs: Obs = null

  override def update(): Unit = if (obs == null) {
    obs = withinOctopus.votesByUser.foreach(votesByUser.value_=)
  }

  private lazy val editor: BindableAction[EditSpace] = new BindableAction(withinOctopus.getTopModule({ case
    m: EditSpace ⇒ m
  }), this)

  private lazy val creatorNameRx = withinOctopus.getCreator.map(_.map(_.getNameOrEmpty()))

  @dom
  override protected def generateHtml(): Binding[Node] = {
    val disabled = findVoteModule.isEmpty
    <div class="card d-inline-flex mt-1 mr-1 flex-column answer">
      <div class="d-inline-flex flex-row flex-nowrap">
        <div class="d-inline-flex flex-column controls">
          <button type="button" class="btn btn-primary btn-sm" disabled={disabled} onclick={upVote _}>Up vote</button>
          <button type="button" class="btn btn-primary btn-sm" disabled={disabled} onclick={downVote _}>Down
            vote</button>
          <span>
            {withinOctopus.votes.dom.bind}
            votes</span>
        </div>
        <div class="card-body">
          <h6 class="card-title">proposal</h6>
          <h6 class="card-subtitle mb-2 text-muted">by
            {creatorNameRx.dom.bind}
          </h6>
          <p class="card-text">
            {withinOctopus.getBody.dom.bind}
          </p>
        </div>
      </div>

      <div class="card-controls-bottom d-inline-flex flex-column">
        {displayVotesByUser(votesByUser.bind).bind}<div class="d-inline-flex">
        {ChangeParent.displayActiveOnly(withinOctopus).bind}{editor.dom.bind}
      </div>
      </div>
    </div>
  }

  //      {sorted foreach {uv => displayVotesBySingleUser(uv._1, uv._2).bind }}
  @dom
  private def displayVotesByUser(votes: Map[Option[User], Int]): Binding[Node] = {
    val sorted = votes.toList.sortBy(_._2).reverse
    <div class="d-inline-flex vote-breakdown">
      <span class={if (votes.isEmpty) "d-none" else ""}>votes:</span>{for (uv <- sorted) yield
      displayVotesBySingleUser(uv._1, uv._2).bind}
    </div>
  }

  @dom
  private def displayVotesBySingleUser(u: Option[User], v: Int): Binding[Node] = u map { user ⇒
    val uname = Var("")
    user.getNameOrEmpty.foreach(uname.value_=)
    <span>
      {uname.bind}
      (
      {v}
      )</span>
  } getOrElse DisplayModel.nospan.bind

  private lazy val votesByUser = Var[Map[Option[User], Int]](Map())


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

