package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Vars
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.connection.Child
import life.plenty.model.modifiers.OctopusModifier
import life.plenty.model.octopi.Octopus
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
  private val siblingModules: Vars[DisplayModule[Octopus]] = Vars()

  override def update(): Unit = {
    //    println("modular display updating", this, getSiblingModules(this), siblingModules, siblingModules.value)
    siblingModules.value.clear()
    siblingModules.value.insertAll(0, getSiblingModules(this).reverse)
  }

  @dom
  override protected def generateHtml(overrides: List[ModuleOverride]): Binding[Node] = {
    //    println("modular display gen HTML", this)

    val displayable = siblingModules map { m ⇒ m.display(this, siblingOverrides ::: overrides)
    } withFilter (_.nonEmpty) map (_.get)

    <div class="modular-display-box">
      {for (d <- displayable) yield d.bind}
    </div>
  }

  private def siblingOverrides = getSiblingModules(this) flatMap (m ⇒ {
    if (m.doDisplay()) m.overrides else Nil
  })
}


class ChildDisplay(override val withinOctopus: Octopus) extends DisplayModule[Octopus] {
  private lazy val modifiers: List[OctopusModifier[Octopus]] =
    withinOctopus.getModules({ case m: OctopusModifier[Octopus] ⇒ m })

  protected val children: Vars[Octopus] = Vars[Octopus]()

  override def update(): Unit = {
    //    println("child display updatding", this)
    children.value.clear()
    children.value.insertAll(0, getChildren)
  }

  @dom
  override protected def generateHtml(overrides: List[ModuleOverride]): Binding[Node] = {
    //    println("child display has children", this, children.value)
    <div class="child-display-box d-flex flex-column">
      {for (c <- children) yield DisplayModel.display(c, overrides ::: getOverridesBelow).bind}
    </div>
  }

  def getChildren: List[Octopus] = {
    //println("getting children", withinOctopus)
    //println("getting children", withinOctopus.connections)
    val children = withinOctopus.connections.collect({ case Child(c: Octopus) ⇒ c })
    //    println("getting children", children)
    modifiers.foldLeft(children)((cs, mod) ⇒ {
      mod.apply(cs): List[Octopus]
    })
  }

  protected def getOverridesBelow = {
    //    println("getting child overrides")
    getSiblingModules(this) flatMap (m ⇒ {
      if (m.doDisplay()) m.overrides else Nil
    })
  }
}

abstract class GroupedChildDisplay(private val _withinOctopus: Octopus) extends ChildDisplay(_withinOctopus) {
  protected val displayInOrder: List[String]

  protected def groupBy(o: Octopus): String

  override def overrides: List[ModuleOverride] = {
    ModuleOverride(this, new NoDisplay(withinOctopus), dm ⇒ {
      dm.isInstanceOf[ChildDisplay] && dm.withinOctopus == withinOctopus
    }) :: super.overrides
  }

  @dom
  override protected def generateHtml(overrides: List[ModuleOverride]): Binding[Node] = {
    val grouped = children.value.groupBy(groupBy)
    val overridesBelow = overrides ::: getOverridesBelow

    println("groups", displayInOrder)
    println("groups", grouped)

    <div class="child-display-grouped-box d-flex flex-row">
      {for (gName ← displayInOrder) yield generateHtmlForGroup(gName, grouped(gName).toList, overridesBelow).bind}
    </div>
  }

  @dom
  private def generateHtmlForGroup(name: String, octopi: List[Octopus],
                                   overridesBelow: List[ModuleOverride]): Binding[Node] = {
    <div class={s"group-$name d-flex flex-column"}>
      {for (c <- octopi) yield DisplayModel.display(c, overridesBelow).bind}
    </div>
  }
}

