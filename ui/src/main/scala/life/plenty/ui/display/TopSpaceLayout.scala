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

  //      {displayModules(siblingModules.withFilter(_.isInstanceOf[SingleActionModuleDisplay[_]]), "top-space-menu")
  // .bind}

  @dom
  override protected def generateHtml(): Binding[Node] = {
    val cos: Seq[ModuleOverride] = this.cachedOverrides.bind
    implicit val os = cos.toList ::: siblingOverrides ::: overrides
    val titleClasses = "title ml-2 " + {if (isConfirmed().bind) "confirmed" else ""}

    val sections: List[Binding[Div]] = sectionsExtension ::: (
      displayHubs(getMembers(children), "administrative section") ::
        displayHubs(getQuestions(children), "questions section") ::
        displayHubs(getProposals(children), "answers section") ::
        displayHubs(getSubSpaces(children), "sub-spaces section") :: Nil)

    def nav(dir: Int)(e: Event) = {
      val s = document.getElementsByClassName("section")(0).asInstanceOf[Div]
      window.scrollBy(s.offsetWidth.toInt * dir, 0)
    }

    <div class="top-space-layout">

      {val menuBar = siblingModules.withFilter(_.isInstanceOf[MenuBar])
    for (m <- menuBar) yield m.display(this, os) map {_.bind} getOrElse DisplayModel.nospan.bind}


      <span class="d-flexflex-column span-separator">

      <div class="row flex-column">
        <h3 class={titleClasses}>
          {hub.getTitle.dom.bind}
        </h3>
        <h5 class="sub-title mt-1 ml-2 text-muted">
          {GraphUtils.getBody(hub).dom.bind}
        </h5>{displayModules(siblingModules.withFilter(_.isInstanceOf[SpaceActionsBar]), "top-space-menu").bind}
      </div>

      <div class="section-nav-buttons">
        <div class="btn btn-large" onclick={nav(-1) _}>
          <span class="oi oi-arrow-thick-left"></span>
        </div>
        <div class="btn btn-large" onclick={nav(1) _}>
          <span class="oi oi-arrow-thick-right"></span>
        </div>
      </div>

      <div class="top-space-child-display row">
        {for (s <- sections) yield s.bind}

        <script>
          positionSectionNav()
        </script>

      </div>

    </span>
    </div>
  }

  protected def sectionsExtension(implicit overrides: List[ModuleOverride]): List[Binding[Div]] = Nil
}



//{singleMountPoint.bind; ""}


//<script>
//function resize() {
//var bs = document.getElementsByClassName("section-nav-buttons")[0]
//var w = window.innerWidth - bs.offsetWidth
//bs.style = "width:" + w + "px;"
//console.log(bs.scrollLeft, bs.offsetWidth, window.innerWidth)
//
//</script>