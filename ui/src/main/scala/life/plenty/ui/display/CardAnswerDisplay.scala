package life.plenty.ui.display

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.{Answer, Contribution, Proposal}
import life.plenty.model.utils.GraphUtils._
import life.plenty.ui.display.actions.AnswerControls
import life.plenty.ui.display.info.AnswerInfo
import life.plenty.ui.display.meta.LayoutModule
import life.plenty.ui.display.utils.CardNavigation
import life.plenty.ui.model.utils.Helpers._
import life.plenty.ui.model.ModuleOverride
import org.scalajs.dom.Node

import scalaz.std.list._
import scalaz.std.option._

//{displayModules(siblingModules.withFilter(_.isInstanceOf[SpaceActionsBar]), "card-space-menu").bind}

class CardAnswerDisplay(override val withinOctopus: Answer) extends LayoutModule[Answer] with CardNavigation {
  override def doDisplay() = true
  private val creator = new OptBindableHub(withinOctopus.getCreator, this)

  @dom
  override protected def generateHtml(): Binding[Node] = {
    val cos: Seq[ModuleOverride] = this.cachedOverrides.bind
    implicit val os = cos.toList ::: siblingOverrides
    val cssClass = if (withinOctopus.isInstanceOf[Proposal]) "answer" else
    if (withinOctopus.isInstanceOf[Contribution]) "contribution" else ""

    <div class={"card d-inline-flex flex-column " + cssClass } id={withinOctopus.id}>
      <span class="d-flex header-block">
        <span class="d-flex title-block" onclick={navigateTo _}>
          <h5 class="card-title">
            {if (withinOctopus.isInstanceOf[Proposal]) "proposal" else
          if (withinOctopus.isInstanceOf[Contribution]) "helper" else ""}
          </h5>
          <div class="card-subtitle">
            {creator.dom.bind}
            <span>{withinOctopus.votes.dom.bind} votes</span>
            {displayModules(siblingModules.withFilter(_.isInstanceOf[AnswerInfo]), "modules").bind}
          </div>
        </span>

        <span class="card-controls">
          <div class="btn btn-primary btn-sm open-btn" onclick={navigateTo _}>open</div>{ // keep break
          displayModules(siblingModules.withFilter(_.isInstanceOf[AnswerControls]), "modules").bind}
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
