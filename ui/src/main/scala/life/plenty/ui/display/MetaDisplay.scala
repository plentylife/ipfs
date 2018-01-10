package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Vars
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.Octopus
import life.plenty.model.connection.Child
import life.plenty.ui.model.DisplayModel
import life.plenty.ui.model.DisplayModel.{DisplayModule, ModuleOverride, getSiblingModules}
import org.scalajs.dom.raw.Node

import scalaz.std.list._


class NoDisplay(override val withinOctopus: Octopus) extends DisplayModule[Octopus] {
  override def doDisplay(): Boolean = false
  override protected def generateHtml(overrides: List[ModuleOverride]): Binding[Node] = null
  override def update(): Unit = Unit
}

class ModularDisplay(override val withinOctopus: Octopus) extends DisplayModule[Octopus] {

  private val siblingModules = Vars[DisplayModule[Octopus]]()
  @dom
  override protected def generateHtml(overrides: List[ModuleOverride]): Binding[Node] = {
    val displayable = siblingModules map (m ⇒ m.display(this, overrides)) withFilter (_.nonEmpty) map (_.get)

    <div>
      {for (d <- displayable) yield d.bind}
    </div>
  }
  override def update(): Unit = {
    println("modular display updating", this)
    for (
      (d: DisplayModule[Octopus], i: Int) ← getSiblingModules(this).reverse.zipWithIndex) {
      siblingModules.value(i) = d
    }
  }
}


class ChildDisplay(override val withinOctopus: Octopus) extends DisplayModule[Octopus] {

  private val children: Vars[Octopus] = Vars[Octopus]()

  override def update(): Unit = {
    println("child display updatding", this)
    for ((c, i) ← getChildren.zipWithIndex) {
      children.value(i) = c
    }
  }
  @dom
  override protected def generateHtml(overrides: List[ModuleOverride]): Binding[Node] = {
    println("child display has children", this, children.value)
    <div>
      {for (c <- children) yield DisplayModel.display(c, overrides ::: childOverrides).bind}
    </div>
  }

  def getChildren: List[Octopus] = {
    //println("getting children", withinOctopus)
    //println("getting children", withinOctopus.connections)
    withinOctopus.connections.collect({ case Child(c: Octopus) ⇒ c })
  }
  private def childOverrides = getSiblingModules(this) flatMap (_.overrides)
}
