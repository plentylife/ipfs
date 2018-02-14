package life.plenty.ui.display.actions

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.definition.Hub
import life.plenty.ui.model.DisplayModel.DisplayModule
import org.scalajs.dom.Node

class CreateAnswerButton(override val withinOctopus: Hub) extends DisplayModule[Hub]{
  override def update(): Unit = Unit

  @dom
  override protected def generateHtml(): Binding[Node] = {
    <div class="btn btn-lg btn-primary">Answer</div>
  }
}
