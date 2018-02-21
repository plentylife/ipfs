package life.plenty.ui.display.actions

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.actions.{ActionCreateAnswer, ActionSignup}
import life.plenty.model.octopi.definition.Hub
import life.plenty.ui
import life.plenty.ui.display.utils.ModalFormAction
import life.plenty.ui.model.DisplayModel.{ActionDisplay, SingleActionModuleDisplay}
import life.plenty.ui.model.UiContext
import life.plenty.ui.display.utils.InputVarWithTextarea
import org.scalajs.dom.{Event, Node}

class SignupButton(override val withinOctopus: Hub) extends ActionDisplay[Hub] {
  private lazy val module: Option[ActionSignup] = withinOctopus.getTopModule(
    {case m: ActionSignup => m})

  @dom
  override def activeDisplay: Binding[Node] = {
    <div class="btn btn-danger" onclick={e: Event ⇒
      module.get.designup(UiContext.getUser)
    }>de-signup</div>
  }

  @dom
  override def inactiveDisplay: Binding[Node] = {<div class="btn btn-info" onclick={e: Event ⇒
    module.get.signup(UiContext.getUser)
  }>signup</div>}

  override def update(): Unit = isEmpty.value_=(module.isEmpty)
}