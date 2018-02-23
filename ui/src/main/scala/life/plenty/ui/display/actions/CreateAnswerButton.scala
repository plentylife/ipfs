package life.plenty.ui.display.actions

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.actions.{ActionCreateAnswer, ActionCreateQuestion}
import life.plenty.model.octopi.definition.{Hub, Module}
import life.plenty.ui
import life.plenty.ui.display.utils.{InputVarWithTextarea, ModalFormAction, StringInputVar}
import life.plenty.ui.model.DisplayModel.{DisplayModule, SingleActionModuleDisplay}
import life.plenty.ui.display.utils.InputVarWithTextarea
import org.scalajs.dom.{Event, Node}

class CreateAnswerButton(override val hub: Hub) extends SingleActionModuleDisplay[Hub] with
ModalFormAction{
  override protected lazy val module: Option[ActionCreateAnswer] = hub.getTopModule(
    {case m: ActionCreateAnswer => m})

  override def update(): Unit = Unit

  @dom
  override protected def presentGenerateHtml(): Binding[Node] = {
    <div class="btn btn-info" onclick={onClick _}>answer</div>
  }

  override protected val formSubmitValue: String = "answer"
  override protected val formCssClass: String = "create-answer-form"

  private val body = new StringInputVar

  @dom
  override protected def createDialog(): Binding[Node] = <span>
    {new InputVarWithTextarea(body, "Answer").dom.bind}
  </span>

  override protected def onSubmit(e: Event): Unit = {
    ui.console.trace(s"submit answer ${body.get}")
    for (b ← body.get) {
      module.get.create(b)
      onSubmitSuccess()
    }
  }
}

//  @dom
//  private def contributionDialog(): Binding[Node] = <span>
//    <h6>Is this a contribution?</h6>
//    <div class="muted">If you are offering to something, whether a serivce, an item, or anything else that
//      requires </div>
//  </span>