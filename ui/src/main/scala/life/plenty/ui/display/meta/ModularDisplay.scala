package life.plenty.ui.display.meta

import com.thoughtworks.binding.Binding.Vars
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.hub.definition.Hub
import life.plenty.ui.console
import life.plenty.ui.model.DisplayModel.{ActionDisplay, getSiblingModules}
import life.plenty.ui.model.{DisplayModule, ModuleOverride, SimpleModuleOverride}
import org.scalajs.dom.raw.Node
import scalaz.std.list._

@deprecated
trait ModularDisplayTrait extends DisplayModule[Hub] {
  override val hub: Hub

  protected val siblingModules: Vars[DisplayModule[Hub]] = Vars()

  override def update(): Unit = {
    //    println("new modules", getSiblingModules(this))
    // fixme the filter might need to go
    val sms = getSiblingModules(this).filterNot(_.isInstanceOf[ActionDisplay[_]]).reverse
    siblingModules.value.clear()
    siblingModules.value.insertAll(0, sms)
    console.trace(s"modular display updating $this $hub $sms overrides ${this.cachedOverrides.value}")
  }

  protected def siblingOverrides = getSiblingModules(this) flatMap (m ⇒ {
    if (m.doDisplay()) m.overrides else Nil
  })

}

@deprecated
class ModularDisplay(override val hub: Hub) extends ModularDisplayTrait {
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

}