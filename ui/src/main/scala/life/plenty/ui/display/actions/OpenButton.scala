package life.plenty.ui.display.actions

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.actions.ActionGiveThanks
import life.plenty.model.hub.{Contribution, Space}
import life.plenty.model.hub.definition.Hub
import life.plenty.ui
import life.plenty.ui.display.utils.{CardNavigation, ModalFormAction}
import life.plenty.ui.model.{DisplayModule, Router, SimpleDisplayModule, UiContext}
import life.plenty.ui.display.utils.InputVarWithDisplay
import org.scalajs.dom.{Event, Node}

trait CardControls extends DisplayModule[Space]

class OpenButton(override val hub: Space) extends CardControls {

  override def update(): Unit = Unit

  override protected def generateHtml(): Binding[Node] = OpenButton.html(hub)
}

object OpenButton extends SimpleDisplayModule[Space] {
  @dom
  override def html(what: Space): Binding[Node] = {
    <div class="btn btn-primary btn-sm open-btn" onclick={e: Event ⇒ Router.navigateToHub(what)}>open</div>
  }
  override def fits(what: Any): Boolean = what.isInstanceOf[Space]
}