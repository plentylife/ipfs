package life.plenty.ui.display.meta

import com.thoughtworks.binding.Binding
import life.plenty.model.octopi.definition.Hub
import life.plenty.ui.model.DisplayModel.DisplayModule
import org.scalajs.dom.raw.Node

class NoDisplay(override val hub: Hub) extends DisplayModule[Hub] {
  override def doDisplay(): Boolean = false

  override def update(): Unit = Unit

  override protected def generateHtml(): Binding[Node] = null
}
