package life.plenty.ui.display

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.definition.Hub
import life.plenty.model.octopi.{Question, Space}
import life.plenty.ui.model.DisplayModule
import life.plenty.ui.display.utils.Helpers._
import org.scalajs.dom.Node

trait InlineDisplay extends DisplayModule[Space] {
  override def doDisplay() = true

  protected val cssClass: String

  @dom
  override protected def generateHtml(): Binding[Node] = {
    <div class={cssClass} id={hub.id}>
      {hub.getTitle.dom.bind}
    </div>
  }

  override def update(): Unit = Unit
}

class InlineQuestionDisplay(override val hub: Question) extends InlineDisplay {
  override protected val cssClass: String = "inline-question"
}

class InlineSpaceDisplay(override val hub: Space) extends InlineDisplay {
  override protected val cssClass: String = "inline-space"
}