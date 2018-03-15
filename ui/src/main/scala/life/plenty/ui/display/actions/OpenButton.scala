package life.plenty.ui.display.actions

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.actions.ActionGiveThanks
import life.plenty.model.hub.{Contribution, Space}
import life.plenty.model.hub.definition.Hub
import life.plenty.ui
import life.plenty.ui.display.utils.{CardNavigation, ModalFormAction}
import life.plenty.ui.model.DisplayModule
import life.plenty.ui.model.UiContext
import life.plenty.ui.display.utils.InputVarWithDisplay
import org.scalajs.dom.{Event, Node}

trait CardControls extends DisplayModule[Space]

class OpenButton(override val hub: Space) extends CardControls with CardNavigation {

  override def update(): Unit = Unit

  @dom
  override protected def generateHtml(): Binding[Node] = <div
  class="btn btn-primary btn-sm open-btn" onclick={navigateTo _}>open</div>
}