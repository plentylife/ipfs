package life.plenty.ui.display

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.Space
import life.plenty.ui.model.DisplayModel
import life.plenty.ui.model.DisplayModel.DisplayModule
import org.scalajs.dom.raw.Node

class RateEffortDisplay(override val withinOctopus: Space) extends DisplayModule[Space] {
  override def update(): Unit = Unit

  @dom
  override protected def generateHtml(overrides: List[DisplayModel.ModuleOverride]): Binding[Node] = {
    <div>
      rating effort eh?
    </div>
  }
}
