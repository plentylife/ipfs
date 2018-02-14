package life.plenty.ui.display

import com.thoughtworks.binding.Binding.{BindingSeq, Vars}
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.connection.Child
import life.plenty.model.modifiers.OctopusModifier
import life.plenty.model.octopi.{ContainerSpace, Members, Space}
import life.plenty.model.octopi.definition.Hub
import life.plenty.ui.console
import life.plenty.ui.display.meta.{ChildDisplay, ModularDisplay, ModularDisplayTrait}
import life.plenty.ui.model.{DisplayModel, UiContext}
import life.plenty.ui.model.DisplayModel.{ActionDisplay, DisplayModule, ModuleOverride, getSiblingModules}
import org.scalajs.dom.raw.Node
import rx.{Obs, Rx}

import scala.collection.mutable
import scalaz.std.option._
import scalaz.std.list._

class TopSpaceDisplay(override val withinOctopus: Space) extends LayoutModule[Space] {

  override def doDisplay(): Boolean = UiContext.startingSpace.value.exists(_.id == withinOctopus.id)

  def getMembers(cs: BindingSeq[Hub]): BindingSeq[Hub]#WithFilter = cs.withFilter({
    case m: Members ⇒ true
    case _ ⇒ false
  })

  def getSubSpaces(cs: BindingSeq[Hub]) = cs.withFilter(_.isInstanceOf[ContainerSpace])


  @dom
  override protected def generateHtml(): Binding[Node] = {
    val cos: Seq[ModuleOverride] = this.cachedOverrides.bind
    implicit val os = cos.toList ::: siblingOverrides

    <div class="top-space-display">

      {
      val menuBar = siblingModules.withFilter(_.isInstanceOf[MenuBar])
      for (m <- menuBar) yield m.display(this, os) map {_.bind} getOrElse DisplayModel.nospan.bind
      }

      <div class="top-space-menu">

      </div>

    <div class="top-space-child-display">
      {displayList(getMembers(children), "administrative").bind}
      <div class="questions">

      </div>
      {displayList(getSubSpaces(children), "sub-spaces").bind}
    </div>

    </div>
  }
}

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
  protected def displayList(seq: BindingSeq[Hub]#WithFilter, cssClass: String)(implicit os: List[ModuleOverride])
  :Binding[Node] = {
    console.trace(s"Layout display list $seq")
    <div class={cssClass}>
      {for (c <- seq) yield DisplayModel.display(c, os, Option(this)).bind}
    </div>
  }

  protected def siblingOverrides: List[ModuleOverride] = getSiblingModules(this) flatMap (m ⇒ {
    if (m.doDisplay()) m.overrides else Nil
  })
}