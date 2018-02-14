package life.plenty.ui.display

import com.thoughtworks.binding.Binding.BindingSeq
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.{ContainerSpace, Members, Space}
import life.plenty.model.octopi.definition.Hub
import life.plenty.ui.display.meta.ChildDisplay
import life.plenty.ui.model.{DisplayModel, UiContext}
import life.plenty.ui.model.DisplayModel.ModuleOverride
import org.scalajs.dom.raw.Node

import scala.collection.mutable
import scalaz.std.option._
import scalaz.std.list._

class TopSpaceChildDisplay(override val withinOctopus: Space) extends ChildDisplay(withinOctopus) {

  override def doDisplay(): Boolean = UiContext.startingSpace.value.exists(_.id == withinOctopus.id)

  def getMembers(cs: BindingSeq[Hub]): BindingSeq[Hub]#WithFilter = cs.withFilter({
    case m: Members ⇒ true
    case _ ⇒ false
  })

  def getSubSpaces(cs: BindingSeq[Hub]) = cs.withFilter(_.isInstanceOf[ContainerSpace])


  @dom
  override protected def generateHtml(): Binding[Node] = {
    implicit val os = getOverridesBelow
    <div class="top-space-child-display">
      {displayList(getMembers(children), "administrative").bind}
      <div class="questions">

      </div>
      {displayList(getSubSpaces(children), "sub-spaces").bind}
    </div>
  }
}
