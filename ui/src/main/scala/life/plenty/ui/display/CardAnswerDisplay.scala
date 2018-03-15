package life.plenty.ui.display

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.hub.{Answer, Contribution, Proposal}
import life.plenty.model.utils.GraphUtils; import life.plenty.model.utils.GraphExtractors
import life.plenty.model.utils.GraphUtils._
import life.plenty.ui.display.actions.{AnswerControls, CardControls}
import life.plenty.ui.display.info.AnswerInfo
import life.plenty.ui.display.meta.LayoutModule
import life.plenty.ui.display.utils.CardNavigation
import life.plenty.ui.display.utils.Helpers._
import life.plenty.ui.model.ModuleOverride
import org.scalajs.dom.Node

import scalaz.std.list._
import scalaz.std.option._

//{displayModules(siblingModules.withFilter(_.isInstanceOf[SpaceActionsBar]), "card-space-menu").bind}

class CardAnswerDisplay(override val hub: Answer) extends LayoutModule[Answer] with CardNavigation {
  override def doDisplay() = true
  private val creator = new OptBindableHub(hub.getCreator, this)
  private lazy val isConfirmed: BasicBindable[Boolean] = GraphExtractors.markedConfirmed(hub)

  @dom
  override protected def generateHtml(): Binding[Node] = {
    val cos: Seq[ModuleOverride] = this.cachedOverrides.bind
    implicit val os = cos.toList ::: siblingOverrides
    val cssClass = if (hub.isInstanceOf[Proposal]) "answer" else {
    if (hub.isInstanceOf[Contribution]) "contribution" else ""}
    var confirmedCss = if (isConfirmed().bind) " confirmed " else ""

    <div class={"card d-inline-flex flex-column " + cssClass + confirmedCss} id={hub.id}>
      <span class="d-flex header-block">
        <span class="d-flex title-block" onclick={navigateTo _}>
          <h5 class="card-title">
            {if (hub.isInstanceOf[Proposal]) "proposal" else
          if (hub.isInstanceOf[Contribution]) "contribution" else ""}
          </h5>
          <div class="card-subtitle">
            {creator.dom.bind}
            <span>{hub.votes.dom.bind} votes</span>
            {displayModules(siblingModules.withFilter(_.isInstanceOf[AnswerInfo]), "modules").bind}
          </div>
        </span>

        <span class="card-controls">
          {displayModules(siblingModules.withFilter(m =>
          m.isInstanceOf[AnswerControls] || m.isInstanceOf[CardControls]), "modules").bind}
        </span>
      </span>

      <div class="card-body">
        {getBody(hub).dom.bind}
      </div>

      {// we can put info from a different module here, and then the question display will simply override them
      ""
       }

    </div>
  }
}

//<div class="btn btn-primary btn-sm open-btn" onclick={navigateTo _}>open</div>
//{displayHubs(children.withFilter(_.isInstanceOf[Answer]), "answers").bind}
