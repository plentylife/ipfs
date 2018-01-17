package life.plenty.ui.display.meta

import com.thoughtworks.binding.Binding.Vars
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.Octopus
import life.plenty.ui.model.DisplayModel.{DisplayModule, ModuleOverride, getSiblingModules}
import org.scalajs.dom.raw.Node

import scalaz.std.list._

class ModularDisplay(override val withinOctopus: Octopus) extends DisplayModule[Octopus] {
  protected val siblingModules: Vars[DisplayModule[Octopus]] = Vars()

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

  protected def siblingOverrides = getSiblingModules(this) flatMap (m ⇒ {
    if (m.doDisplay()) m.overrides else Nil
  })
}


abstract class GroupedModularDisplay(private val _withinOctopus: Octopus) extends ModularDisplay(_withinOctopus) {
  protected val displayInOrder: List[String]

  protected def groupBy(o: DisplayModule[_]): String

  @dom
  override protected def generateHtml(passedOverrides: List[ModuleOverride]): Binding[Node] = {
    val grouped = siblingModules.value.groupBy(groupBy)
    val overridesBelow = passedOverrides ::: siblingOverrides ::: overrides

    <div class="modular-display-box">
      {for (gName ← displayInOrder) yield generateHtmlForGroup(gName, grouped(gName).toList, overridesBelow).bind}
    </div>
  }

  override def overrides: List[ModuleOverride] = {
    ModuleOverride(this, new NoDisplay(withinOctopus), dm ⇒ {
      dm.isInstanceOf[ModularDisplay] && dm.withinOctopus == withinOctopus
      //      dm.isInstanceOf[ModularDisplay]
    }) :: super.overrides
  }

  @dom
  private def generateHtmlForGroup(name: String, modules: List[DisplayModule[_]],
                                   overridesBelow: List[ModuleOverride]): Binding[Node] = {
    val displayable = modules map { m ⇒ m.display(this, overridesBelow) } withFilter (_.nonEmpty) map (_.get)

    <div class={s"group-$name"}>
      {for (d <- displayable) yield d.bind}
    </div>
  }
}