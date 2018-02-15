package life.plenty.ui.display.actions

import com.thoughtworks.binding.Binding.{BindingSeq, Vars}
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.actions.ActionCreateQuestion
import life.plenty.model.octopi.definition.Hub
import life.plenty.ui
import life.plenty.ui.display.Modal
import life.plenty.ui.model.DisplayModel.SingleActionModuleDisplay
import life.plenty.ui.model.Helpers._
import org.scalajs.dom.{Event, Node}

class CreateQuestionButton(override val withinOctopus: Hub) extends SingleActionModuleDisplay[Hub]
  with ModalFormAction {
  override protected lazy val module: Option[ActionCreateQuestion] =
    withinOctopus.getTopModule({ case m: ActionCreateQuestion => m })

  override def update(): Unit = Unit

  @dom
  override protected def presentGenerateHtml(): Binding[Node] = {
    <div class="btn btn-primary" onclick={onClick _}>ask a question</div>
  }

  private val title = new InputVar
  private val description = new InputVar

  override protected val formCssClass: String = "create-question-form"
  override protected val formSubmitValue: String = "ask"

  @dom
  override protected def createDialog(): Binding[Node] = {
    <span>
      {new InputVarWithDisplay(title, "Question title").dom.bind}
      {new InputVarWithTextarea(description, "Question " + "description").dom.bind}
    </span>
  }

  override protected def onSubmit(e: Event): Unit = {
    ui.console.trace(s"submit question ${title.get} ${description.get}")
    for (t ← title.get; d <- description.get) {
      module.get.create(t, d)
      onSubmitSuccess()
    }
  }
}

trait ModalFormAction {
  protected def onClick(e: Event): Unit = Modal.setContentAndOpen(_createDialog())

  @dom
  private def _createDialog(): Binding[Node] = {
    <form class={"d-flex flex-column align-items-center " + formCssClass} onsubmit={_onSubmit _}>
      {createDialog().bind}<input type="submit" class="btn btn-primary" value={formSubmitValue}/>
    </form>
  }

  private def _onSubmit(e: Event): Unit = {
    e.preventDefault()
    onSubmit(e)
  }

  protected val formSubmitValue: String
  protected val formCssClass: String

  protected def onSubmitSuccess(): Unit = Modal.close()

  protected def createDialog(): Binding[Node]

  protected def onSubmit(e: Event): Unit
}