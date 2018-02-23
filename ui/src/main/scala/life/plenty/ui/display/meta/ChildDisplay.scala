package life.plenty.ui.display.meta

import com.thoughtworks.binding.Binding.{BindingSeq, Vars}
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.connection.Child
import life.plenty.model.modifiers.OctopusModifier
import life.plenty.model.octopi.definition.Hub
import life.plenty.ui.console
import life.plenty.ui.model.{DisplayModel, ModuleOverride, SimpleModuleOverride}
import life.plenty.ui.model.DisplayModel.{DisplayModule, getSiblingModules}
import org.scalajs.dom.raw.Node
import rx.{Obs, Rx}

import scala.language.postfixOps
import scalaz.std.list._

class ChildDisplay(override val hub: Hub) extends DisplayModule[Hub] {

  private lazy val modifiers: List[OctopusModifier[Hub]] = {
    console.trace(s"child meta getting modifiers from ${hub.modules}")
    hub.getModules({ case m: OctopusModifier[Hub] ⇒ m })
  }

  protected val children: Vars[Hub] = Vars[Hub]()
  protected var rxChildren: Obs = null

  override def update(): Unit = {
    console.trace(s"child update ${hub} $this rxChildren ${rxChildren}")
    if (rxChildren == null) {
      rxChildren = getChildren.foreach { cs ⇒
        children.value.clear()
        children.value.insertAll(0, cs)
        console.trace(s"child updated ${hub} $cs")
      }
    }
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

  @dom
  protected def displayList(seq: BindingSeq[Hub]#WithFilter, cssClass: String)(implicit os: List[ModuleOverride])
  :Binding[Node] = {
    <div class={cssClass}>
      {for (c <- seq) yield DisplayModel.display(c, os, Option(this)).bind}
    </div>
  }

  @dom
  override protected def generateHtml(): Binding[Node] = {
    //    println("child display gen html", this, children.value)
    val os = getOverridesBelow
    <div class="child-display-box d-flex flex-column flex-wrap">
      {for (c <- children) yield DisplayModel.display(c, os, Option(this)).bind}
    </div>
  }

  protected def getOverridesBelow = {
    //    println("getting child overrides")
    getSiblingModules(this) flatMap (m ⇒ {
      if (m.doDisplay()) m.overrides else Nil
    })
  }
}

abstract class GroupedChildDisplay(private val _withinOctopus: Hub) extends ChildDisplay(_withinOctopus) {
  protected val displayInOrder: List[String]
  protected val titles: Map[String, String] = Map()

  override def overrides: List[ModuleOverride] = {
    SimpleModuleOverride(this, new NoDisplay(hub), dm ⇒ {
      dm.isInstanceOf[ChildDisplay] && !dm.isInstanceOf[GroupedChildDisplay] && dm.hub == hub
    }) :: super.overrides
  }

  protected def groupBy(o: Hub): String

  @dom
  override protected def generateHtml(): Binding[Node] = {
    val overridesBelow = overrides ::: getOverridesBelow

    <div class="child-display-grouped-box d-flex flex-column flex-wrap">
      {val grouped = children.bind.groupBy(groupBy)
    console.trace(s"Grouped Child Display genHtml ${children.value}")

    for (gName ← displayInOrder) yield {
      val octopi = grouped.get(gName).map(_.toList).getOrElse(List())
      generateHtmlForGroup(gName, octopi, overridesBelow).bind
    }}
    </div>
  }


  @dom
  private def generateHtmlForGroup(name: String, octopi: List[Hub],
                                   overridesBelow: List[ModuleOverride]): Binding[Node] = if (octopi.nonEmpty) {
    {console.println(s"generateHtmlForGroup $name $titles ${titles.get(name)}")}
    <div class={s"d-flex group-$name flex-wrap flex-column"}>
      {if (titles.get(name).nonEmpty) {
      <h3 class="subsection-header">
        {titles.get(name).getOrElse("")}
      </h3>
    } else {<span class="d-none"></span>}}{for (c <- octopi) yield DisplayModel.display(c, overridesBelow).bind}
    </div>
  } else {
    <span class="d-none"></span>
  }
}