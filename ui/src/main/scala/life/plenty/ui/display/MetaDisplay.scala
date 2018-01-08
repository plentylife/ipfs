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
    val bindings = Vars[Binding[Node]]()
    val modules = getSiblingModules(this).reverse.zipWithIndex map { indexed ⇒
      val (m: DisplayModule[Octopus], i: Int) = indexed
      m.setUpdater((b) ⇒ bindings.value(i) = b)
      m.display(overrides)
    } flatten;

    //    println("mod disp ", bindings)

    <div>
      {for (b <- bindings) yield b.bind}
    </div>
  }
}


class ChildDisplay(override val withinOctopus: Octopus) extends DisplayModule[Octopus] {
  @dom
  override protected def generateHtml(overrides: List[ModuleOverride]): Binding[Node] = {
    val children = withinOctopus.connections.collect { case Child(c: Octopus) ⇒
      c
    }
    val childVars = Vars(children: _*)
    val boundingVars = Vars[Binding[Node]]()
    for ((v, i) ← childVars.value.zipWithIndex) {
      val upd = (b: Binding[Node]) ⇒ {boundingVars.value(i); println("child display updater")}: Unit
      boundingVars.value(i) = DisplayModel.display(v, overrides ::: childOverrides, upd)
    }

    println("child disp of ", withinOctopus, children, withinOctopus.connections)

    <div>
      {for (b <- boundingVars) yield b.bind}
    </div>
  }
  private def childOverrides = getSiblingModules(this) flatMap (_.overrides)
}
