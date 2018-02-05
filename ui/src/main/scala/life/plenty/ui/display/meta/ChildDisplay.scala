package life.plenty.ui.display.meta

import com.thoughtworks.binding.Binding.Vars
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.connection.Child
import life.plenty.model.modifiers.OctopusModifier
import life.plenty.model.octopi.Octopus
import life.plenty.ui.console
import life.plenty.ui.model.DisplayModel
import life.plenty.ui.model.DisplayModel.{DisplayModule, ModuleOverride, getSiblingModules}
import org.scalajs.dom.raw.Node
import rx.{Obs, Rx}

import scala.language.postfixOps
import scalaz.std.list._

class ChildDisplay(override val withinOctopus: Octopus) extends DisplayModule[Octopus] {

  private lazy val modifiers: List[OctopusModifier[Octopus]] = {
    console.trace(s"child meta getting modifiers from ${withinOctopus.modules}")
    withinOctopus.getModules({ case m: OctopusModifier[Octopus] ⇒ m })
  }

  protected val children: Vars[Octopus] = Vars[Octopus]()
  protected var rxChildren: Obs = null

  override def update(): Unit = {
    console.trace(s"child update ${withinOctopus} $this rxChildren ${rxChildren}")
    if (rxChildren == null) {
      rxChildren = getChildren.foreach { cs ⇒
        children.value.clear()
        children.value.insertAll(0, cs)
        console.trace(s"child updated ${withinOctopus} $cs")
      }
    }
  }


  def getChildren: Rx[List[Octopus]] = {
    console.trace(s"getting children ${withinOctopus}")
    //    val childrenRx: Rx[List[Octopus]] = withinOctopus.rx.cons.debounce(100 millis)
    //      .map(_.collect({ case Child(c: Octopus) ⇒ c }))
    val childrenRx: Rx[List[Octopus]] = withinOctopus.rx.getAll({ case Child(c: Octopus) ⇒ c })
    val ordered = modifiers.foldLeft(childrenRx)((cs, mod) ⇒ {
      console.trace(s"applying modifiers ${withinOctopus}")
      mod.applyRx(cs): Rx[List[Octopus]]
    })
    ordered
  }

  @dom
  override protected def generateHtml(): Binding[Node] = {
    //    println("child display gen html", this, children.value)
    <div class="child-display-box d-flex flex-column flex-wrap">
      {for (c <- children) yield DisplayModel.display(c, overrides ::: getOverridesBelow, Option(this)).bind}
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
  protected val titles: Map[String, String] = Map()

  override def overrides: List[ModuleOverride] = {
    ModuleOverride(this, new NoDisplay(withinOctopus), dm ⇒ {
      dm.isInstanceOf[ChildDisplay] && dm.withinOctopus == withinOctopus
    }) :: super.overrides
  }

  protected def groupBy(o: Octopus): String

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
  private def generateHtmlForGroup(name: String, octopi: List[Octopus],
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