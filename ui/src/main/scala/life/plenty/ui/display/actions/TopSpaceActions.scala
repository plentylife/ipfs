package life.plenty.ui.display.actions

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.Space
import life.plenty.ui.model.DisplayModel.{ActionDisplay, DisplayModule}
import org.scalajs.dom.{Event, Node}

class TopSpaceActions (override val withinOctopus: Space) extends DisplayModule[Space] {
  override def update(): Unit = Unit

  @dom
  override protected def generateHtml(): Binding[Node] = {
    <div class="d-inline-flex top-space-actions">
      <div class="btn btn-primary btn-large" onclick={onCreateSpace _}>Create space</div>
    </div>
  }

  private def onCreateSpace(e: Event) = {
    CreateSpace.openInModal(withinOctopus)
  }
}
