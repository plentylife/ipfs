package life.plenty.ui.display

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.Space
import life.plenty.ui.model.DisplayModule
import org.scalajs.dom.raw.Node

class RateEffortDisplay(override val hub: Space) extends DisplayModule[Space] {
  override def update(): Unit = Unit

  @dom
  override protected def generateHtml(): Binding[Node] = {
    <div>
      rating effort eh?
    </div>
  }
}
