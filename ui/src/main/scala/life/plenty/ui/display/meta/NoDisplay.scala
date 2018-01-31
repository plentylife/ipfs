package life.plenty.ui.display.meta

import com.thoughtworks.binding.Binding
import life.plenty.model.octopi.Octopus
import life.plenty.ui.model.DisplayModel.DisplayModule
import org.scalajs.dom.raw.Node

class NoDisplay(override val withinOctopus: Octopus) extends DisplayModule[Octopus] {
  override def doDisplay(): Boolean = false

  override def update(): Unit = Unit

  override protected def generateHtml(): Binding[Node] = null
}
