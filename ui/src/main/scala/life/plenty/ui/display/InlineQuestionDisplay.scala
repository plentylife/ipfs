package life.plenty.ui.display

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.Question
import life.plenty.ui.model.DisplayModel.DisplayModule
import life.plenty.ui.display.utils.Helpers._
import org.scalajs.dom.Node

class InlineQuestionDisplay(override val hub: Question) extends DisplayModule[Question] {
  override def doDisplay() = true

  @dom
  override protected def generateHtml(): Binding[Node] = {
    <div class="inline-question" id={hub.id}>
      {hub.getTitle.dom.bind}
    </div>
  }

  override def update(): Unit = Unit
}