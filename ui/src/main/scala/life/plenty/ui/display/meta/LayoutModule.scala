package life.plenty.ui.display.meta

import com.thoughtworks.binding.Binding.{BindingSeq, Vars}
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.connection.Child
import life.plenty.model.modifiers.OctopusModifier
import life.plenty.model.octopi.definition.Hub
import life.plenty.ui.console
import life.plenty.ui.model.{DisplayModel, ModuleOverride}
import life.plenty.ui.model.DisplayModel.{DisplayModule, getSiblingModules}
import org.scalajs.dom.raw.Node
import rx.{Obs, Rx}

import scalaz.std.option._
import scalaz.std.list._

trait LayoutModule[T <: Hub] extends DisplayModule[T] {

  protected val siblingModules: Vars[DisplayModule[Hub]] = Vars()
  protected val children: Vars[Hub] = Vars[Hub]()
  protected var rxChildren: Obs = null

  override def update(): Unit = {
    val sms = getSiblingModules(this)
    siblingModules.value.clear()
    siblingModules.value.insertAll(0, sms)
    if (rxChildren == null) {
      rxChildren = getChildren.foreach { cs ⇒
        children.value.clear()
        children.value.insertAll(0, cs)
        console.trace(s"child updated ${withinOctopus} $cs")
      }
    }
    console.trace(s"layout display updating $this $withinOctopus $sms overrides ${this.cachedOverrides.value}")
  }

  private lazy val modifiers: List[OctopusModifier[Hub]] = {
    console.trace(s"child meta getting modifiers from ${withinOctopus.modules}")
    withinOctopus.getModules({ case m: OctopusModifier[Hub] ⇒ m })
  }

  private def getChildren: Rx[List[Hub]] = {
    console.trace(s"getting children ${withinOctopus} ${withinOctopus.id}")
    val childrenRx: Rx[List[Hub]] = withinOctopus.rx.getAll({ case Child(c: Hub) ⇒ c })
    val ordered = modifiers.foldLeft(childrenRx)((cs, mod) ⇒ {
      console.trace(s"applying modifiers ${withinOctopus}")
      mod.applyRx(cs): Rx[List[Hub]]
    })
    ordered
  }

  @dom
  protected def displayHubs(seq: BindingSeq[Hub]#WithFilter, cssClass: String)(implicit os: List[ModuleOverride])
  :Binding[Node] = {
    console.trace(s"Layout display list (hubs) $seq")
    val displays = for (c <- seq) yield DisplayModel.display(c, os, Option(this))
    val hideClass = if (displays.bind.isEmpty) "d-none" else ""
    <div class={cssClass + " " + hideClass}>
      {for (d ← displays) yield d.bind}
    </div>
  }

  @dom
  protected def displayModules(seq: BindingSeq[DisplayModule[_]]#WithFilter, cssClass: String)
                              (implicit os: List[ModuleOverride]) :Binding[Node] = {
    console.trace(s"Layout display list (modules) $seq")
    <div class={cssClass}>
      {for (m <- seq) yield m.display(this, os) map {_.bind} getOrElse DisplayModel.nospan.bind}
    </div>
  }

  protected def siblingOverrides: List[ModuleOverride] = getSiblingModules(this) flatMap (m ⇒ {
    if (m.doDisplay()) m.overrides else Nil
  })
}
