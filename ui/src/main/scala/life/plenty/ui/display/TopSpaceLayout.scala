package life.plenty.ui.display

import com.thoughtworks.binding.Binding.{BindingSeq, Vars}
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.connection.Child
import life.plenty.model.modifiers.OctopusModifier
import life.plenty.model.octopi._
import life.plenty.model.octopi.definition.Hub
import life.plenty.model.utils.ConFinders
import life.plenty.ui.console
import life.plenty.ui.display.actions.SpaceActionsBar
import life.plenty.ui.display.meta.{ChildDisplay, LayoutModule, ModularDisplay, ModularDisplayTrait}
import life.plenty.ui.model.{DisplayModel, ModuleOverride, UiContext}
import life.plenty.ui.model.DisplayModel.{ActionDisplay, DisplayModule, SingleActionModuleDisplay, getSiblingModules}
import org.scalajs.dom.raw.Node
import rx.{Obs, Rx}

import scala.collection.mutable
import scalaz.std.option._
import scalaz.std.list._
import life.plenty.ui.model.utils.Helpers._

class TopSpaceLayout(override val withinOctopus: Space) extends LayoutModule[Space] {

  override def doDisplay(): Boolean = UiContext.startingSpace.value.exists(_.id == withinOctopus.id)

  def getMembers(cs: BindingSeq[Hub]): BindingSeq[Hub]#WithFilter = cs.withFilter({
    case m: Members ⇒ true
    case _ ⇒ false
  })

  def getSubSpaces(cs: BindingSeq[Hub]) = cs.withFilter(_.isInstanceOf[ContainerSpace])
  def getQuestions(cs: BindingSeq[Hub]) = cs.withFilter(_.isInstanceOf[Question])
  def getProposals(cs: BindingSeq[Hub]) = cs.withFilter(_.isInstanceOf[Proposal])
  def getContributions(cs: BindingSeq[Hub]) = cs.withFilter(_.isInstanceOf[Contribution])

//      {displayModules(siblingModules.withFilter(_.isInstanceOf[SingleActionModuleDisplay[_]]), "top-space-menu").bind}

  @dom
  override protected def generateHtml(): Binding[Node] = {
    val cos: Seq[ModuleOverride] = this.cachedOverrides.bind
    implicit val os = cos.toList ::: siblingOverrides ::: overrides

    <div class="top-space-layout">
      {
      val menuBar = siblingModules.withFilter(_.isInstanceOf[MenuBar])
      for (m <- menuBar) yield m.display(this, os) map {_.bind} getOrElse DisplayModel.nospan.bind
      }


      <span class="d-flex flex-column span-separator">
          <div class="row flex-column">
          <h3 class="title ml-2">
            {withinOctopus.getTitle.dom.bind}
          </h3>
            <h5 class="sub-title mt-1 ml-2 text-muted">
              {ConFinders.getBody(withinOctopus).dom.bind}
            </h5>

          {displayModules(siblingModules.withFilter(_.isInstanceOf[SpaceActionsBar]), "top-space-menu").bind}
          </div>

        <div class="top-space-child-display row">
          {for (s <- sectionsExtension) yield s.bind}
          {displayHubs(getMembers(children), "administrative section").bind}
          {displayHubs(getQuestions(children), "questions section").bind}
          {displayHubs(getProposals(children), "answers section").bind}
          {displayHubs(getSubSpaces(children), "sub-spaces section").bind}
        </div>

        </span>
    </div>
  }

  protected def sectionsExtension(implicit overrides: List[ModuleOverride]): List[Binding[Node]]  = Nil
}

