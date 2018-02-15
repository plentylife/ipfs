package life.plenty.ui.display.actions

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.actions.{ActionCreateAnswer, ActionCreateQuestion}
import life.plenty.model.octopi.definition.{Hub, Module}
import life.plenty.ui.model.DisplayModel.{DisplayModule, SingleActionModuleDisplay}
import org.scalajs.dom.Node

class CreateAnswerButton(override val withinOctopus: Hub) extends SingleActionModuleDisplay[Hub] {
  override protected lazy val module: Option[Module[Hub]] = withinOctopus.getTopModule(
    {case m: ActionCreateAnswer => m})

  override def update(): Unit = Unit

  @dom
  override protected def presentGenerateHtml(): Binding[Node] = {
    <div class="btn btn-primary">answer</div>
  }
}
