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
import rx.async.Platform._
import rx.async._
import rx.{Ctx, Obs, Rx}

import scala.concurrent.duration._
import scala.language.postfixOps
import scalaz.std.list._

class ChildDisplay(override val withinOctopus: Octopus) extends DisplayModule[Octopus] {
  implicit val ctx: Ctx.Owner = Ctx.Owner.safe()

  private lazy val modifiers: List[OctopusModifier[Octopus]] = {
    console.trace(s"child meta getting modifiers from ${withinOctopus.modules}")
    withinOctopus.getModules({ case m: OctopusModifier[Octopus] ⇒ m })
  }

  protected val children: Vars[Octopus] = Vars[Octopus]()
  protected var rxChildren: Obs = null

  override def update(): Unit = {
    console.println(s"child updatde ${withinOctopus} $this rxChildren ${rxChildren}")
    if (rxChildren == null) {
      rxChildren = getChildren.foreach { cs ⇒
        children.value.clear()
        children.value.insertAll(0, cs)
      }
    }
  }


  def getChildren: Rx[List[Octopus]] = {
    console.println(s"getting children ${withinOctopus}")
    val childrenRx: Rx[List[Octopus]] = withinOctopus.rx.cons.debounce(1000 millis)
      .map(_.collect({ case Child(c: Octopus) ⇒ c }))
    val ordered = modifiers.foldLeft(childrenRx)((cs, mod) ⇒ {
      console.println(s"applying modifiers ${withinOctopus}")
      mod.applyRx(cs): Rx[List[Octopus]]
    })
    ordered
  }

  @dom
  override protected def generateHtml(overrides: List[ModuleOverride]): Binding[Node] = {
    //    println("child display gen html", this, children.value)
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