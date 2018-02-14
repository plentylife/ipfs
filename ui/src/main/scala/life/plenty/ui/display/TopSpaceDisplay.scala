package life.plenty.ui.display

import com.thoughtworks.binding.Binding.BindingSeq
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.{Members, Space}
import life.plenty.model.octopi.definition.Hub
import life.plenty.ui.display.meta.ChildDisplay
import life.plenty.ui.model.DisplayModel
import life.plenty.ui.model.DisplayModel.ModuleOverride
import org.scalajs.dom.raw.Node

import scala.collection.mutable
import scalaz.std.option._
import scalaz.std.list._

class TopSpaceChildDisplay(override val withinOctopus: Space) extends ChildDisplay(withinOctopus) {

  def getMembers(cs: BindingSeq[Hub]) = cs.withFilter({
    case m: Members ⇒ true
    case _ ⇒ false
  })

  @dom
  override protected def generateHtml(): Binding[Node] = {
    val os = getOverridesBelow
    <div class="top-space-child-display">
      <div class="administrative">
        {for (c <- getMembers(children)) yield DisplayModel.display(c, os, Option(this)).bind}
      </div>
    </div>
  }
}
