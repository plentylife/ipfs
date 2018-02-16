package life.plenty.ui.display

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.{Answer, Proposal}
import life.plenty.model.utils.ConFinders._
import life.plenty.ui.display.actions.AnswerControls
import life.plenty.ui.display.meta.LayoutModule
import life.plenty.ui.display.utils.CardNavigation
import life.plenty.ui.model.DisplayModel.ModuleOverride
import life.plenty.ui.model.Helpers._
import org.scalajs.dom.Node

import scalaz.std.list._
import scalaz.std.option._

//{displayModules(siblingModules.withFilter(_.isInstanceOf[SpaceActionsBar]), "card-space-menu").bind}

class CardAnswerDisplay(override val withinOctopus: Answer) extends LayoutModule[Answer] with CardNavigation {
  override def doDisplay() = true

  @dom
  override protected def generateHtml(): Binding[Node] = {
    val cos: Seq[ModuleOverride] = this.cachedOverrides.bind
    implicit val os = cos.toList ::: siblingOverrides

    <div class="card d-inline-flex flex-column answer" id={withinOctopus.id}>
      <span class="d-flex header-block">
        <span class="d-flex title-block" onclick={navigateTo _}>
          <h5 class="card-title">
            {if (withinOctopus.isInstanceOf[Proposal]) "proposal" else ""}
          </h5>
          <div class="card-subtitle">
            {withinOctopus.votes.dom.bind} votes
          </div>
        </span>

        <span class="card-controls">
          <div class="btn btn-primary btn-sm open-btn" onclick={navigateTo _}>open</div>{ // keep break
          displayModules(siblingModules.withFilter(_.isInstanceOf[AnswerControls]), "").bind}
        </span>
      </span>

      <div class="card-body">
        {getBody(withinOctopus).dom.bind}
      </div>

      {// we can put info from a different module here, and then the question display will simply override them
      ""
       }

    </div>
  }
}

//{displayHubs(children.withFilter(_.isInstanceOf[Answer]), "answers").bind}
