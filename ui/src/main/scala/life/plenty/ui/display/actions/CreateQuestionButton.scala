package life.plenty.ui.display.actions

import com.thoughtworks.binding.Binding.{BindingSeq, Vars}
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.actions.ActionCreateQuestion
import life.plenty.model.octopi.definition.Hub
import life.plenty.ui
import life.plenty.ui.display.Modal
import life.plenty.ui.display.utils._
import life.plenty.ui.model.DisplayModel.SingleActionModuleDisplay
import life.plenty.ui.display.utils.Helpers._
import life.plenty.ui.model.utils._
import org.scalajs.dom.{Event, Node}

class CreateQuestionButton(override val hub: Hub) extends SingleActionModuleDisplay[Hub]
  with ModalFormAction {
  override protected lazy val module: Option[ActionCreateQuestion] =
    hub.getTopModule({ case m: ActionCreateQuestion => m })

  override def update(): Unit = Unit

  @dom
  override protected def presentGenerateHtml(): Binding[Node] = {
    <div class="btn btn-primary" onclick={onClick _}>ask a question</div>
  }

  private val isSignup = new BooleanInputVar
  private val title = new StringInputVar
  private val description = new StringInputVar

  override protected val formCssClass: String = "create-question-form"
  override protected val formSubmitValue: String = "ask"

  @dom
  override protected def createDialog(): Binding[Node] = {
    <span>
      {new InputVarWithCheckbox(isSignup, "This is a sign-up list").dom.bind}
      {new InputVarWithDisplay(title, "Question title").dom.bind}
      {new InputVarWithTextarea(description, "Question " + "description").dom.bind}
    </span>
  }

  override protected def onSubmit(e: Event): Unit = {
    ui.console.trace(s"submit question ${title.get} ${description.get}")
    for (t ← title.get; d <- description.get) {
      module.get.create(t, d, isSignup.get.getOrElse(false)) // if never had input will be None
      onSubmitSuccess()
    }
  }
}

