package life.plenty.ui.display

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.{Child, Octopus}
import life.plenty.ui.model.DisplayModel
import life.plenty.ui.model.DisplayModel.{DisplayModule, ModuleOverride, getSiblingModules}
import org.scalajs.dom.raw.Node


class NoDisplay(override val withinOctopus: Octopus) extends DisplayModule[Octopus] {
  override def doDisplay(): Boolean = false
  override protected def displaySelf(overrides: List[ModuleOverride]): Binding[Node] = null
}

class ModularDisplay(override val withinOctopus: Octopus) extends DisplayModule[Octopus] {
  @dom
  override protected def displaySelf(overrides: List[ModuleOverride]): Binding[Node] = {
    println("mod disp", overrides)
    val bindings: List[Binding[Node]] = getSiblingModules(this) flatMap { m: DisplayModule[Octopus] ⇒
      m.display(overrides)
    }

    <div>
      {for (b <- bindings) yield b.bind}
    </div>
  }
}


class ChildDisplay(override val withinOctopus: Octopus) extends DisplayModule[Octopus] {
  @dom
  override protected def displaySelf(overrides: List[ModuleOverride]): Binding[Node] = {
    println("child overrieds", childOverrides)
    val bindings = withinOctopus.connections.collect { case Child(c: Octopus) ⇒
      DisplayModel.display(c, overrides ::: childOverrides)
    }
    <div>
      {for (b <- bindings) yield b.bind}
    </div>
  }
  private def childOverrides = getSiblingModules(this) flatMap (_.overrides)
}
