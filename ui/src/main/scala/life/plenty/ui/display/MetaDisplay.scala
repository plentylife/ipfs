package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Vars
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.{Child, Octopus}
import life.plenty.ui.model.DisplayModel
import life.plenty.ui.model.DisplayModel.{DisplayModule, ModuleOverride, getSiblingModules}
import org.scalajs.dom.raw.Node

import scalaz.std.list._


class NoDisplay(override val withinOctopus: Octopus) extends DisplayModule[Octopus] {
  override def doDisplay(): Boolean = false
  override protected def generateHtml(overrides: List[ModuleOverride]): Binding[Node] = null
  override protected def updateSelf(): Unit = Unit
}

class ModularDisplay(override val withinOctopus: Octopus) extends DisplayModule[Octopus] {

  private val siblingModules = Vars[DisplayModule[Octopus]]()
  @dom
  override protected def generateHtml(overrides: List[ModuleOverride]): Binding[Node] = {
    updateSelf()

    //    println("mod disp ", bindings)

    <div>
      {for {m <- siblingModules; d ← m.display(this, overrides)} yield d.bind}
    </div>
  }
  override protected def updateSelf(): Unit = for ((d, i) ← getSiblingModules(this).reverse) {
    siblingModules.value(i) = d
  }
}


class ChildDisplay(override val withinOctopus: Octopus) extends DisplayModule[Octopus] {

  private val children: Vars[Octopus] = Vars[Octopus]()

  @dom
  override protected def generateHtml(overrides: List[ModuleOverride]): Binding[Node] = {
    updateSelf()
    println("child disp of ", withinOctopus, children, withinOctopus.connections)

    <div>
      {for (c <- children) yield DisplayModel.display(c, overrides ::: childOverrides).bind}
    </div>
  }

  override def updateSelf(): Unit = for ((c, i) ← getChildren.zipWithIndex) {
    println("child display updatding")
    children.value(i) = c
  }

  def getChildren: List[Octopus] = {
    println("getting children", withinOctopus)
    println("getting children", withinOctopus.connections)
    withinOctopus.connections.collect({ case Child(c: Octopus) ⇒ c })
  }
  private def childOverrides = getSiblingModules(this) flatMap (_.overrides)
}
