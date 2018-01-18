package life.plenty.ui.display.meta

import com.thoughtworks.binding.Binding.Vars
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.connection.Child
import life.plenty.model.modifiers.OctopusModifier
import life.plenty.model.octopi.Octopus
import life.plenty.ui.model.DisplayModel
import life.plenty.ui.model.DisplayModel.{DisplayModule, ModuleOverride, getSiblingModules}
import org.scalajs.dom.raw.Node

import scalaz.std.list._

class ChildDisplay(override val withinOctopus: Octopus) extends DisplayModule[Octopus] {
  private lazy val modifiers: List[OctopusModifier[Octopus]] =
    withinOctopus.getModules({ case m: OctopusModifier[Octopus] ⇒ m })

  protected val children: Vars[Octopus] = Vars[Octopus]()

  override def update(): Unit = {
    //    println("child display updatding", this)
    children.value.clear()
    children.value.insertAll(0, getChildren)
  }

  def getChildren: List[Octopus] = {
    //println("getting children", withinOctopus)
    //println("getting children", withinOctopus.connections)
    val children = withinOctopus.connections.collect({ case Child(c: Octopus) ⇒ c })
    //    println("getting children", children)
    val ordered = modifiers.foldLeft(children)((cs, mod) ⇒ {
      mod.apply(cs): List[Octopus]
    })
    println("got children", this, ordered)
    ordered
  }

  @dom
  override protected def generateHtml(overrides: List[ModuleOverride]): Binding[Node] = {
    //    println("child display has children", this, children.value)
    <div class="child-display-box d-flex flex-column">
      {for (c <- children) yield DisplayModel.display(c, overrides ::: getOverridesBelow).bind}
    </div>
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

  override def overrides: List[ModuleOverride] = {
    ModuleOverride(this, new NoDisplay(withinOctopus), dm ⇒ {
      dm.isInstanceOf[ChildDisplay] && dm.withinOctopus == withinOctopus
    }) :: super.overrides
  }

  protected def groupBy(o: Octopus): String

  @dom
  override protected def generateHtml(overrides: List[ModuleOverride]): Binding[Node] = {
    val grouped = children.bind.groupBy(groupBy)
    val overridesBelow = overrides ::: getOverridesBelow

    <div class="child-display-grouped-box d-flex flex-row">
      {for (gName ← displayInOrder) yield {
      val octopi = grouped.get(gName).map(_.toList).getOrElse(List())
      generateHtmlForGroup(gName, octopi, overridesBelow).bind
    }}
    </div>
  }

  @dom
  private def generateHtmlForGroup(name: String, octopi: List[Octopus],
                                   overridesBelow: List[ModuleOverride]): Binding[Node] = {
    <div class={s"d-flex group-$name"}>
      {for (c <- octopi) yield DisplayModel.display(c, overridesBelow).bind}
    </div>
  }
}