package life.plenty.ui.display.actions

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.actions.ActionCreateAnswer
import life.plenty.model.hub.definition.{Hub, Module}
import life.plenty.ui.model.DisplayModel.SingleActionModuleDisplay
import org.scalajs.dom.Node

class CreateSpaceButton(override val hub: Hub) extends SingleActionModuleDisplay[Hub] {
  // because the module is global, and isn't actually a module
  override protected lazy val module: Option[Module[Hub]] = Some(null)

  override def update(): Unit = Unit

  @dom
  override protected def presentGenerateHtml(): Binding[Node] = {
    <div class="btn btn-lg btn-primary">Create space</div>
  }
}
