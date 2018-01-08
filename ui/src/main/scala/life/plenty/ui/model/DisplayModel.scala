package life.plenty.ui.model

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model._
import org.scalajs.dom.raw.Node

import scala.language.postfixOps
import scalaz.std.list._

object DisplayModel {

  def display(o: Octopus, overrides: List[ModuleOverride] = List()): Binding[Node] = {
    println("disp function over", o, overrides)
    o.modules.collectFirst({ case dm: DisplayModule[_] ⇒ dm.display(overrides)
    }).flatten getOrElse noDisplay
  }


  def getSiblingModules(self: DisplayModule[Octopus]): List[DisplayModule[Octopus]] = self.withinOctopus.getModules {
    case m: DisplayModule[_] if m != self ⇒ m }

  @dom
  private def noDisplay: Binding[Node] = <div>This octopus has no display</div>

  trait DisplayModule[+T <: Octopus] extends Module[T] {
    def doDisplay(): Boolean = true

    def overrides: List[ModuleOverride] = List()

    def display(overrides: List[ModuleOverride] = List()): Option[Binding[Node]] = {
      overriddenBy(overrides) match {
        case Some(module) ⇒ module.display(overrides)
        case _ ⇒ if (doDisplay()) Option(displaySelf(overrides)) else None
      }
    }

    protected def displaySelf(overrides: List[ModuleOverride]): Binding[Node]

    private def overriddenBy(overrides: List[ModuleOverride]): Option[DisplayModule[_]] =
      overrides.collectFirst {
        case ModuleOverride(by, con) if con(this) ⇒ by
      }
  }

  case class ModuleOverride(by: DisplayModule[Octopus], condition: (DisplayModule[Octopus]) ⇒ Boolean)

  trait TitleDisplay extends DisplayModule[Octopus]
}