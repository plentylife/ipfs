package life.plenty.ui.display.meta

import com.thoughtworks.binding.Binding.{BindingSeq, Vars}
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.connection.Child
import life.plenty.model.modifiers.OctopusModifier
import life.plenty.model.octopi.definition.Hub
import life.plenty.ui.console
import life.plenty.ui.model.{DisplayModel, DisplayModule, ModuleOverride}
import life.plenty.ui.model.DisplayModel.getSiblingModules
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.Node
import rx.{Obs, Rx, Var ⇒ rxVar}
import scalaz.std.option._
import scalaz.std.list._

trait LayoutModule[T <: Hub] extends DisplayModule[T] {

  protected val siblingModules: Vars[DisplayModule[Hub]] = Vars()
  protected val children: Vars[Hub] = Vars[Hub]()
  protected var rxChildren: Rx[List[Hub]] = null //fixme use ListBindable
  protected var rxChildrenObs: Obs = null

  override def update(): Unit = {
    val sms = getSiblingModules(this)
    siblingModules.value.clear()
    siblingModules.value.insertAll(0, sms)
    if (rxChildrenObs == null) {
      rxChildren = getChildren
      rxChildrenObs = rxChildren.foreach { cs ⇒
        children.value.clear()
        children.value.insertAll(0, cs)
        console.trace(s"child updated ${hub} $cs")
      }
    }
    console.trace(s"layout display updating $this $hub $sms overrides ${this.cachedOverrides.value}")
  }

  private lazy val modifiers: List[OctopusModifier[Hub]] = {
    console.trace(s"child meta getting modifiers from ${hub.modules}")
    hub.getModules({ case m: OctopusModifier[Hub] ⇒ m })
  }

  private def getChildren: Rx[List[Hub]] = {
    console.trace(s"getting children ${hub} ${hub.id}")
    val childrenRx: Rx[List[Hub]] = hub.rx.getAll({ case Child(c: Hub) ⇒ c })
    val ordered = modifiers.foldLeft(childrenRx)((cs, mod) ⇒ {
      console.trace(s"applying modifiers ${hub}")
      mod.applyRx(cs): Rx[List[Hub]]
    })
    ordered
  }

  protected def displayHubs(seq: BindingSeq[Hub]#WithFilter, cssClass: String,
                            header: Binding[Node], ifEmpty: Binding[Node])
                           (implicit os: List[ModuleOverride]):Binding[Node] =
    displayHubsF(seq, cssClass, Option(header), Option(ifEmpty))

  protected def displayHubsF(seq: BindingSeq[Hub]#WithFilter, cssClass: String,
                             header: Option[Binding[Node]] = None, ifEmpty: Option[Binding[Node]] = None)
                            (implicit os: List[ModuleOverride]):Binding[Node] = {
    console.trace(s"Layout display list (hubs) $seq $os")
    val displays: BindingSeq[Binding[Node]] = for (c <- seq) yield DisplayModel.display(c, os, Option(this))
    displayHubNodes(displays, cssClass, header, ifEmpty)
  }

  protected def displayHubs(seq: BindingSeq[Hub], cssClass: String,
                            header: Option[Binding[Node]] = None, ifEmpty: Option[Binding[Node]] = None)
                           (implicit os: List[ModuleOverride]):Binding[Node] = {
    console.trace(s"Layout display list (hubs) $seq $os")
    val displays: BindingSeq[Binding[Node]] = for (c <- seq) yield DisplayModel.display(c, os, Option(this))
    displayHubNodes(displays, cssClass, header, ifEmpty)
  }

  @dom
  protected def displayHubNodes(displays: BindingSeq[Binding[Node]], cssClass: String,
                            header: Option[Binding[Node]] = None, ifEmpty: Option[Binding[Node]] = None)
                           (implicit os: List[ModuleOverride]):Binding[Node] = {
    val hideClass = if (displays.bind.isEmpty && ifEmpty.isEmpty) "d-none" else ""
    <div class={cssClass + " " + hideClass}>
      {header.map(_.bind).getOrElse(DisplayModel.nospan.bind)}
      {for (d ← displays) yield d.bind}
      {if (displays.bind.isEmpty && ifEmpty.nonEmpty) {ifEmpty.get.bind} else DisplayModel.nospan.bind}
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
