package life.plenty.ui.display.actions

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.actions.{ActionCreateAnswer, ActionUpDownVote}
import life.plenty.model.octopi.definition.Hub
import life.plenty.ui
import life.plenty.ui.display.utils.ModalFormAction
import life.plenty.ui.model.DisplayModel.SingleActionModuleDisplay
import org.scalajs.dom.{Event, Node}

trait AnswerControls extends SingleActionModuleDisplay[Hub]

class VoteButtons(override val withinOctopus: Hub) extends AnswerControls {

  override protected lazy val module: Option[ActionUpDownVote] = withinOctopus.getTopModule(
    {case m: ActionUpDownVote => m})

  override def update(): Unit = Unit

  @dom
  override protected def presentGenerateHtml(): Binding[Node] = {
    <span class="voting-controls btn-group">
      <div class="btn btn-info btn-sm" onclick={_: Event ⇒ module.get.up()}>up vote</div>
      <div class="btn btn-info btn-sm" onclick={_: Event ⇒ module.get.down()}>down vote</div>
    </span>
  }

}

//  @dom
//  private def contributionDialog(): Binding[Node] = <span>
//    <h6>Is this a contribution?</h6>
//    <div class="muted">If you are offering to something, whether a serivce, an item, or anything else that
//      requires </div>
//  </span>