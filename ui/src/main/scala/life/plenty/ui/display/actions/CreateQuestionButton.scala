package life.plenty.ui.display.actions

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.actions.ActionCreateQuestion
import life.plenty.model.octopi.definition.{Hub, Module}
import life.plenty.ui.model.DisplayModel.{DisplayModule, SingleActionModuleDisplay}
import org.scalajs.dom.Node

class CreateQuestionButton(override val withinOctopus: Hub) extends SingleActionModuleDisplay[Hub]{
  override protected lazy val module: Option[Module[Hub]] = withinOctopus.getTopModule({case m: ActionCreateQuestion
  => m})

  override def update(): Unit = Unit

  @dom
  override protected def presentGenerateHtml(): Binding[Node] = {
    <div class="btn btn-lg btn-primary">Ask</div>
  }
}
