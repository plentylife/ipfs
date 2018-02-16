package life.plenty.ui.display

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.definition.Hub
import life.plenty.model.octopi.{Answer, Question, Space}
import life.plenty.model.utils.ConFinders._
import life.plenty.ui.display.actions.SpaceActionsBar
import life.plenty.ui.display.meta.LayoutModule
import life.plenty.ui.display.utils.CardNavigation
import life.plenty.ui.model.DisplayModel.DisplayModule
import life.plenty.ui.model.Helpers._
import life.plenty.ui.model.{ComplexModuleOverride, ModuleOverride, Router, UiContext}
import org.scalajs.dom.Node

//{displayModules(siblingModules.withFilter(_.isInstanceOf[SpaceActionsBar]), "card-space-menu").bind}

class CardQuestionDisplay(override val withinOctopus: Question) extends LayoutModule[Question] with CardNavigation {
  override def doDisplay() = true

  @dom
  override protected def generateHtml(): Binding[Node] = {
    val cos: Seq[ModuleOverride] = this.cachedOverrides.bind
    val inlineQuestins =
      ComplexModuleOverride(this, {case m: InlineQuestionDisplay ⇒ m}, _.isInstanceOf[CardQuestionDisplay])
    implicit val os = inlineQuestins :: cos.toList ::: siblingOverrides

    <div class="card d-inline-flex flex-column question" id={withinOctopus.id}>
      <span class="d-flex header-block" onclick={navigateTo _}>
        <span class="d-flex title-block">
          <h3 class="card-title">{withinOctopus.getTitle.dom.bind}</h3>
          <h6 class="card-subtitle mb-2 text-muted">
            {getBody(withinOctopus).dom.bind}
          </h6>
        </span>
        <span class="card-controls">
          <div class="btn btn-primary btn-sm">open</div>
        </span>
      </span>

      <div class="card-body">

        {displayHubs(children.withFilter(_.isInstanceOf[Answer]), "answers").bind}
        {displayHubs(children.withFilter(_.isInstanceOf[Question]), "questions").bind}

        </div>
    </div>
  }
}