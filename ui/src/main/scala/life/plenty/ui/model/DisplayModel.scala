package life.plenty.ui.model

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model._
import org.scalajs.dom.raw.Node

import scala.language.postfixOps
import scalaz.std.list._

object DisplayModel {

  def display(o: Octopus, overrides: List[ModuleOverride] = List()): Binding[Node] = {
    o.modules.collectFirst({ case dm: DisplayModule[_] ⇒
      dm.display(None, overrides)
    }).flatten getOrElse noDisplay
  }

  /* todo. fixme this is flawed */
  def reRender(o: Octopus): Unit = o.getTopModule({ case m: DisplayModule[_] ⇒ m }).foreach(m ⇒ {
    // fixme. add a variable to display module indicating if it has been rendered once
    println("re-render of module", m, m.withinOctopus)
    m.update()
  })

  def getSiblingModules(self: DisplayModule[Octopus]): List[DisplayModule[Octopus]] = self.withinOctopus.getModules {
    case m: DisplayModule[_] if m != self ⇒ m }

  @dom
  private def noDisplay: Binding[Node] = <div>This octopus has no display</div>

  /* the main trait */

  trait DisplayModule[+T <: Octopus] extends Module[T] {
    def display(calledBy: DisplayModule[Octopus], overrides: List[ModuleOverride] = List()): Option[Binding[Node]] = {
      this.display(Option(calledBy), overrides)
    }

    def doDisplay(): Boolean = true

    def overrides: List[ModuleOverride] = List()

    def display(calledBy: Option[DisplayModule[_]], overrides: List[ModuleOverride]): Option[Binding[Node]] = {
      overriddenBy(overrides) match {
        case Some(module) ⇒ module.display(calledBy, overrides)
        case _ ⇒ if (doDisplay()) {
          println("displaying ", this, withinOctopus, calledBy)
          update()
          Option(generateHtml(overrides))
        } else None
      }
    }

    def update(): Unit

    protected def generateHtml(overrides: List[ModuleOverride]): Binding[Node]

    private def overriddenBy(overrides: List[ModuleOverride]): Option[DisplayModule[_]] =
      overrides.collectFirst {
        case ModuleOverride(by, con) if con(this) ⇒ by
      }
  }

  case class ModuleOverride(by: DisplayModule[Octopus], condition: (DisplayModule[Octopus]) ⇒ Boolean)
}