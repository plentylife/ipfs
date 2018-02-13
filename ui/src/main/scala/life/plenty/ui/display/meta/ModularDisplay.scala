package life.plenty.ui.display.meta

import com.thoughtworks.binding.Binding.Vars
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.definition.Hub
import life.plenty.ui.console
import life.plenty.ui.model.DisplayModel.{ActionDisplay, DisplayModule, ModuleOverride, getSiblingModules}
import org.scalajs.dom.raw.Node

import scalaz.std.list._
class ModularDisplay(override val withinOctopus: Hub) extends DisplayModule[Hub] {
  protected val siblingModules: Vars[DisplayModule[Hub]] = Vars()

  override def update(): Unit = {
    //    println("new modules", getSiblingModules(this))
    val sms = getSiblingModules(this).filterNot(_.isInstanceOf[ActionDisplay[_]]).reverse
    siblingModules.value.clear()
    siblingModules.value.insertAll(0, sms)
    console.trace(s"modular display updating $this $withinOctopus $sms overrides ${this.cachedOverrides.value}")
  }

  @dom
  override protected def generateHtml(): Binding[Node] = {
    //    println("modular display gen HTML", this)

    val cos: Seq[ModuleOverride] = this.cachedOverrides.bind
    val displayable = siblingModules map { m ⇒ m.display(this, siblingOverrides ::: cos.toList)
    } withFilter (_.nonEmpty) map (_.get)

    console.trace(s"modular display displaying with sibling overrides ${siblingOverrides}")

    <div class="modular-display-box d-inline-flex flex-column">
      {for (d <- displayable) yield d.bind}
    </div>
  }

  protected def siblingOverrides = getSiblingModules(this) flatMap (m ⇒ {
    if (m.doDisplay()) m.overrides else Nil
  })
}


abstract class GroupedModularDisplay(private val _withinOctopus: Hub) extends ModularDisplay(_withinOctopus) {
  protected val displayInOrder: List[String]

  protected def groupBy(o: DisplayModule[_]): String

  @dom
  override protected def generateHtml(): Binding[Node] = {
    val grouped = siblingModules.value.groupBy(groupBy)
    val overridesBelow = cachedOverrides.bind.toList ::: siblingOverrides ::: overrides

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