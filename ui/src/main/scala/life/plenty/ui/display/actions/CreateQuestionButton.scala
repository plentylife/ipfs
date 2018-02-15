package life.plenty.ui.display.actions

import life.plenty.ui.model.Helpers._
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.actions.ActionCreateQuestion
import life.plenty.model.octopi.definition.{Hub, Module}
import life.plenty.ui
import life.plenty.ui.display.Modal
import life.plenty.ui.display.actions.CreateSpace.onSubmit
import life.plenty.ui.model.DisplayModel.{DisplayModule, SingleActionModuleDisplay}
import org.scalajs.dom.{Event, Node}

class CreateQuestionButton(override val withinOctopus: Hub) extends SingleActionModuleDisplay[Hub]{
  override protected lazy val module: Option[ActionCreateQuestion] = withinOctopus.getTopModule({case m: ActionCreateQuestion
  => m})

  override def update(): Unit = Unit

  @dom
  override protected def presentGenerateHtml(): Binding[Node] = {
    <div class="btn btn-primary" onclick={e: Event ⇒
      Modal.setContentAndOpen(createDialog())
    }>ask a question</div>
  }

  private val title = new InputVar
  private val description = new InputVar

  @dom
  private def createDialog(): Binding[Node] = {
    <form class="d-flex flex-column align-items-center create-question-form" onsubmit={onSubmit _}>
      {new InputVarWithDisplay(title, "Question title").dom.bind}
      {new InputVarWithTextarea(description, "Question description").dom.bind}
      <input type="submit" class="btn btn-primary" value="ask"/>
    </form>
  }

  private def onSubmit(e: Event): Unit = {
    e.preventDefault()
    ui.console.trace(s"submit question ${title.get} ${description.get}")
    for (t ← title.get; d <- description.get) {
      module.get.create(t, d)
      Modal.close()
    }
  }
}
