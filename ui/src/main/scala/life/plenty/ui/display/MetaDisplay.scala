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
}

class ModularDisplay(override val withinOctopus: Octopus) extends DisplayModule[Octopus] {
  @dom
  override protected def generateHtml(overrides: List[ModuleOverride]): Binding[Node] = {
    // for some reason flatMap does not work
    val bindings = getSiblingModules(this).reverse map { m: DisplayModule[Octopus] ⇒
      m.display(overrides)
    } flatten;

    val vars = Vars(bindings: _*)

    //    println("mod disp ", bindings)

    <div>
      {for (b <- vars) yield b.bind}
    </div>
  }
}


class ChildDisplay(override val withinOctopus: Octopus) extends DisplayModule[Octopus] {
  @dom
  override protected def generateHtml(overrides: List[ModuleOverride]): Binding[Node] = {
    val bindings = withinOctopus.connections.collect { case Child(c: Octopus) ⇒
      DisplayModel.display(c, overrides ::: childOverrides)
    }
    val vars = Vars(bindings: _*)

    println("child disp of ", withinOctopus, bindings, withinOctopus.connections)

    <div>
      {for (b <- vars) yield b.bind}
    </div>
  }
  private def childOverrides = getSiblingModules(this) flatMap (_.overrides)
}
