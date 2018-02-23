package life.plenty.ui.display.actions

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.actions.{ActionCreateAnswer, ActionGiveThanks}
import life.plenty.model.octopi.Contribution
import life.plenty.model.octopi.definition.Hub
import life.plenty.ui
import life.plenty.ui.display.utils.{InputVarWithDisplay, ModalFormAction, TransactionalAmountVar}
import life.plenty.ui.model.DisplayModel.SingleActionModuleDisplay
import life.plenty.ui.model.UiContext
import life.plenty.ui.display.utils.{InputVarWithDisplay, InputVarWithTextarea}
import org.scalajs.dom.{Event, Node}

class GiveButton(override val hub: Contribution) extends ModalFormAction with AnswerControls {

  override protected lazy val module: Option[ActionGiveThanks] =
    hub.getTopModule({case m: ActionGiveThanks => m})

  override def update(): Unit = Unit

  @dom
  override protected def presentGenerateHtml(): Binding[Node] = {
    <div class="btn btn-success give-btn" onclick={onClick _}>give {ui.thanks}hanks</div>
  }

  override protected val formSubmitValue: String = "give"
  override protected val formCssClass: String = "give-thanks-form"

  private val thanks = new TransactionalAmountVar

  @dom
  override protected def createDialog(): Binding[Node] = <span>
    {new InputVarWithDisplay(thanks, s"How many ${ui.thanks}hanks",
    s"The amount of ${ui.thanks}hanks has to be more than 0 and a round number").dom.bind}
  </span>

  override protected def onSubmit(e: Event): Unit = {
    ui.console.trace(s"submit give thanks ${thanks.get}")
    for (t ← thanks.get) {
      module.get.add(t, UiContext.getUser)
      onSubmitSuccess()
    }
  }
}