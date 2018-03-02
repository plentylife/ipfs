package life.plenty.ui.display

import com.thoughtworks.binding.Binding.{BindingSeq, SingleMountPoint, Var}
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi._
import life.plenty.model.octopi.definition.Hub
import life.plenty.model.utils.GraphUtils
import life.plenty.ui.display.actions.SpaceActionsBar
import life.plenty.ui.display.meta.LayoutModule
import life.plenty.ui.display.utils.Helpers._
import life.plenty.ui.model.{DisplayModel, ModuleOverride, UiContext}
import org.scalajs.dom.html.Div
import org.scalajs.dom.html.Script
import org.scalajs.dom.raw.{Event, Node}
import org.scalajs.dom.{Element, document, window}

import scala.scalajs.js
import scala.xml.NodeBuffer
import scalaz.std.list._
import scalaz.std.option._

class TopSpaceLayout(override val hub: Space) extends LayoutModule[Space] {

  override def doDisplay(): Boolean = UiContext.startingSpace.value.exists(_.id == hub.id)

  private lazy val isConfirmed: BasicBindable[Boolean] = GraphUtils.markedConfirmed(hub)

  def getMembers(cs: BindingSeq[Hub]): BindingSeq[Hub]#WithFilter = cs.withFilter({
    case m: Members ⇒ true
    case _ ⇒ false
  })

  def getSubSpaces(cs: BindingSeq[Hub]) = cs.withFilter(_.isInstanceOf[ContainerSpace])

  def getQuestions(cs: BindingSeq[Hub]) = cs.withFilter(_.isInstanceOf[Question])

  def getProposals(cs: BindingSeq[Hub]) = cs.withFilter(_.isInstanceOf[Proposal])

  def getContributions(cs: BindingSeq[Hub]) = cs.withFilter(_.isInstanceOf[Contribution])

//  def nav(dir: Int)(e: Event) = {
//    val s = document.getElementsByClassName("section")(0).asInstanceOf[Div]
//    window.scrollBy(s.offsetWidth.toInt * dir, 0)
//  }

  protected val additionalCss = Var("")

  @dom
  override protected def generateHtml(): Binding[Node] = {
    val cos: Seq[ModuleOverride] = this.cachedOverrides.bind
    implicit val os = cos.toList ::: siblingOverrides ::: overrides
    val titleClasses = "title ml-2 " + {if (isConfirmed().bind) "confirmed" else ""}

    <div class={"top-space-layout " + additionalCss.bind}>

      {val menuBar = siblingModules.withFilter(_.isInstanceOf[MenuBar])
    for (m <- menuBar) yield m.display(this, os) map {_.bind} getOrElse DisplayModel.nospan.bind}


      <span class="span-separator top-space-layout-content">

      <div class="layout-header">
        <h3 class={titleClasses}>
          {hub.getTitle.dom.bind}
        </h3>
        <h5 class="sub-title mt-1 ml-2 text-muted">
          {GraphUtils.getBody(hub).dom.bind}
        </h5>{displayModules(siblingModules.withFilter(_.isInstanceOf[SpaceActionsBar]), "top-space-menu").bind}
      </div>

      <div class="section-nav-buttons">
        <div class="btn btn-large nav-left">
          <span class="oi oi-arrow-thick-left"></span>
        </div>
        <div class="btn btn-large nav-right">
          <span class="oi oi-arrow-thick-right"></span>
        </div>
      </div>

        {sectionsDisplay(sections).bind}

      <script>
//        positionSectionNav();
        //fitLayoutContainerToEdge();
      </script>

    </span>
    </div>
  }

  @dom
  protected def sectionsDisplay(sections: List[Binding[Node]]): Binding[Node] = {
    <div class="scroll-wrapper">
      <div class="top-space-child-display row">
        {for (s <- sections) yield s.bind}

        <script>
          addScrollToSectionContainer()
        </script>
      </div>
    </div>
  }

  protected def sections(implicit overrides: List[ModuleOverride]): List[Binding[Node]] =
    displayHubs(getMembers(children), "administrative section") ::
    displayHubs(getQuestions(children), "questions section") ::
    displayHubs(getProposals(children), "answers section") ::
    displayHubs(getSubSpaces(children), "sub-spaces section") :: Nil
}