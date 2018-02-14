package life.plenty.ui.display.actions

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.Space
import life.plenty.ui.model.DisplayModel
import life.plenty.ui.model.DisplayModel.{ActionDisplay, DisplayModule}
import org.scalajs.dom.{Event, Node}
import life.plenty.ui.model.Helpers._
class TopSpaceActions (override val withinOctopus: Space) extends DisplayModule[Space] {
  override def update(): Unit = Unit

  private lazy val ask = new BindableModule(withinOctopus.getTopModule({case m: CreateQuestionButton ⇒ m}), this)

  @dom
  override protected def generateHtml(): Binding[Node] = {
    <div class="d-inline-flex top-space-actions">
      <div class="btn btn-primary btn-large" onclick={onCreateSpace _}>Create space</div>
      {ask.dom.bind}
    </div>
  }

  private def onCreateSpace(e: Event) = {
    CreateSpace.openInModal(withinOctopus)
  }
}
