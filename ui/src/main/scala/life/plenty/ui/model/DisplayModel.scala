package life.plenty.ui.model

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model._
import life.plenty.ui.display.ChildDisplay
import org.scalajs.dom.raw.Node

import scala.language.postfixOps
import scalaz.std.list._

object DisplayModel {

  def display(o: Octopus, overrides: List[ModuleOverride] = List()): Binding[Node] = {
    o.modules.collectFirst({ case dm: DisplayModule[_] ⇒
      dm.display(None, overrides)
    }).flatten getOrElse noDisplay
  }

  def reRender(o: Octopus): Unit = o.getTopConnectionData({ case Parent(p: Octopus) ⇒ p }).foreach {
    _.getTopModule({ case m: ChildDisplay ⇒ m }).foreach(_.updateSelf())
  }


  def getSiblingModules(self: DisplayModule[Octopus]): List[DisplayModule[Octopus]] = self.withinOctopus.getModules {
    case m: DisplayModule[_] if m != self ⇒ m }

  @dom
  private def noDisplay: Binding[Node] = <div>This octopus has no display</div>

  trait DisplayModule[+T <: Octopus] extends Module[T] {
    private var _affects = Set[DisplayModule[_]]()

    def display(calledBy: DisplayModule[Octopus], overrides: List[ModuleOverride] = List()): Option[Binding[Node]] = {
      this.display(Option(calledBy), overrides)
    }

    def doDisplay(): Boolean = true

    def overrides: List[ModuleOverride] = List()

    def display(calledBy: Option[DisplayModule[_]], overrides: List[ModuleOverride]): Option[Binding[Node]] = {
      //      println("displaying ", this, withinOctopus)
      overriddenBy(overrides) match {
        case Some(module) ⇒ module.display(calledBy, overrides)
        case _ ⇒ if (doDisplay()) {
          calledBy foreach (c ⇒ _affects += c)
          Option(generateHtml(overrides))
        } else None
      }
    }

    def update = affects foreach (_.updateSelf())

    def affects = _affects

    protected def updateSelf(): Unit

    protected def generateHtml(overrides: List[ModuleOverride]): Binding[Node]

    private def overriddenBy(overrides: List[ModuleOverride]): Option[DisplayModule[_]] =
      overrides.collectFirst {
        case ModuleOverride(by, con) if con(this) ⇒ by
      }
  }

  case class ModuleOverride(by: DisplayModule[Octopus], condition: (DisplayModule[Octopus]) ⇒ Boolean)
}