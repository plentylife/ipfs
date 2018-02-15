package life.plenty.ui.display

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.{Answer, Question, Space}
import life.plenty.model.utils.ConFinders._
import life.plenty.ui.display.actions.SpaceActionsBar
import life.plenty.ui.display.meta.LayoutModule
import life.plenty.ui.model.DisplayModel.ModuleOverride
import life.plenty.ui.model.Helpers._
import life.plenty.ui.model.UiContext
import org.scalajs.dom.Node

class TopQuestionDisplay(override val withinOctopus: Question) extends LayoutModule[Question] {
  override def doDisplay() = true

  @dom
  override protected def generateHtml(): Binding[Node] = {
    val cos: Seq[ModuleOverride] = this.cachedOverrides.bind
    implicit val os = cos.toList ::: siblingOverrides

    <div class="card d-inline-flex mt-1 mr-1 flex-column space-card" id={withinOctopus.id}>
      <h3 class="card-title">{withinOctopus.getTitle.dom.bind}</h3>
      <h6 class="card-subtitle mb-2 text-muted">
        {getBody(withinOctopus).dom.bind}
      </h6>
      <div class="card-body">
        {displayModules(siblingModules.withFilter(_.isInstanceOf[SpaceActionsBar]), "card-space-menu").bind}

        {displayHubs(children.withFilter(_.isInstanceOf[Answer]), "answers").bind}

        </div>
    </div>
  }
}
