package life.plenty.ui.display.actions

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.actions.ActionCreateQuestion
import life.plenty.model.octopi.definition.{Hub, Module}
import life.plenty.ui.display.Modal
import life.plenty.ui.display.actions.CreateSpace.onSubmit
import life.plenty.ui.model.DisplayModel.{DisplayModule, SingleActionModuleDisplay}
import org.scalajs.dom.{Event, Node}

class CreateQuestionButton(override val withinOctopus: Hub) extends SingleActionModuleDisplay[Hub]{
  override protected lazy val module: Option[Module[Hub]] = withinOctopus.getTopModule({case m: ActionCreateQuestion
  => m})

  override def update(): Unit = Unit

  @dom
  override protected def presentGenerateHtml(): Binding[Node] = {
    <div class="btn btn-lg btn-primary" onclick={e: Event ⇒
      Modal.setContentAndOpen(createDialog())
    }>Ask</div>
  }

  @dom
  private def createDialog(): Binding[Node] = {
    <form class="d-flex flex-column align-items-center create-question-form" onsubmit={onSubmit _}>
      create question
    </form>
  }

  private def onSubmit(e: Event) = ???
}
