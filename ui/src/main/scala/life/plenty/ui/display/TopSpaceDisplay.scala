package life.plenty.ui.display

import com.thoughtworks.binding.Binding.{BindingSeq, Vars}
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.connection.Child
import life.plenty.model.modifiers.OctopusModifier
import life.plenty.model.octopi.{ContainerSpace, Members, Space}
import life.plenty.model.octopi.definition.Hub
import life.plenty.ui.console
import life.plenty.ui.display.actions.SpaceActionsBar
import life.plenty.ui.display.meta.{ChildDisplay, LayoutModule, ModularDisplay, ModularDisplayTrait}
import life.plenty.ui.model.{DisplayModel, UiContext}
import life.plenty.ui.model.DisplayModel.{ActionDisplay, DisplayModule, ModuleOverride, SingleActionModuleDisplay, getSiblingModules}
import org.scalajs.dom.raw.Node
import rx.{Obs, Rx}

import scala.collection.mutable
import scalaz.std.option._
import scalaz.std.list._

class TopSpaceDisplay(override val withinOctopus: Space) extends LayoutModule[Space] {

  override def doDisplay(): Boolean = UiContext.startingSpace.value.exists(_.id == withinOctopus.id)

  def getMembers(cs: BindingSeq[Hub]): BindingSeq[Hub]#WithFilter = cs.withFilter({
    case m: Members ⇒ true
    case _ ⇒ false
  })

  def getSubSpaces(cs: BindingSeq[Hub]) = cs.withFilter(_.isInstanceOf[ContainerSpace])

//      {displayModules(siblingModules.withFilter(_.isInstanceOf[SingleActionModuleDisplay[_]]), "top-space-menu").bind}

  @dom
  override protected def generateHtml(): Binding[Node] = {
    val cos: Seq[ModuleOverride] = this.cachedOverrides.bind
    implicit val os = cos.toList ::: siblingOverrides

    <div class="top-space-display">

      {
      val menuBar = siblingModules.withFilter(_.isInstanceOf[MenuBar])
      for (m <- menuBar) yield m.display(this, os) map {_.bind} getOrElse DisplayModel.nospan.bind
      }

      {displayModules(siblingModules.withFilter(_.isInstanceOf[SpaceActionsBar]), "top-space-menu").bind}

    <div class="top-space-child-display">
      {displayHubs(getMembers(children), "administrative").bind}
      <div class="questions">

      </div>
      {displayHubs(getSubSpaces(children), "sub-spaces").bind}
    </div>

    </div>
  }
}

