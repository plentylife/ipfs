package life.plenty.ui.display

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.{Answer, Question, Space}
import life.plenty.model.utils.GraphUtils.getBody
import life.plenty.ui.display.actions.{AnswerControls, CardControls}
import life.plenty.ui.display.meta.LayoutModule
import life.plenty.ui.display.utils.CardNavigation
import life.plenty.ui.model.{ComplexModuleOverride, ModuleOverride}
import life.plenty.ui.model.utils.Helpers._
import org.scalajs.dom.Node

class CardSpaceDisplay(override val withinOctopus: Space) extends LayoutModule[Space] with CardNavigation {
  override def doDisplay() = !sameAsUiStarting(withinOctopus)

  @dom
  override protected def generateHtml(): Binding[Node] = {
    val cos: Seq[ModuleOverride] = this.cachedOverrides.bind
    val inlineQuestins =
      ComplexModuleOverride(this, {case m: InlineQuestionDisplay ⇒ m}, _.isInstanceOf[CardQuestionDisplay])
    implicit val os = inlineQuestins :: cos.toList ::: siblingOverrides
    val cssClass = ""

    <div class={"card d-inline-flex flex-column space " + cssClass} id={withinOctopus.id}>
      <span class="d-flex header-block">
        <span class="d-flex title-block" onclick={navigateTo _}>
          <h5 class="card-title">
            {withinOctopus.getTitle.dom.bind}
          </h5>

          <div class="card-subtitle">
            {getBody(withinOctopus).dom.bind}
          </div>
        </span>

        <span class="card-controls">
          {displayModules(siblingModules.withFilter(m =>
          m.isInstanceOf[AnswerControls] || m.isInstanceOf[CardControls]), "modules").bind}
        </span>
      </span>

      <div class="card-body">
        {displayHubs(children.withFilter(_.isInstanceOf[Question]), "inner-questions").bind}
      </div>

    </div>
  }
}


//<div class="card-body">
//</div>{// we can put info from a different module here, and then the question display will simply override them
//  ""}
