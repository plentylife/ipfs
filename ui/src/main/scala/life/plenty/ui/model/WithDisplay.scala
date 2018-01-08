package life.plenty.ui.model

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model._
import org.scalajs.dom.raw.Node

import scala.language.postfixOps
import scalaz.std.list._

object DisplayModuleDefinitions {

  def display(o: Octopus, overrides: List[ModuleOverride] = List()): Binding[Node] = {
    println("disp function over", o, overrides)
    o.modules.collectFirst({ case dm: DisplayModule[_] ⇒ dm.display(overrides)
    }).flatten getOrElse {noDisplay}
  }


  def getSiblingModules(self: DisplayModule[Octopus]): List[DisplayModule[Octopus]] = self.withinOctopus.getModules {
    case m: DisplayModule[_] if m != self ⇒ m }

  @dom
  private def noDisplay: Binding[Node] = <div>This octopus has no display</div>

  trait DisplayModule[+T <: Octopus] extends Module[T] {
    def doDisplay: Boolean = true

    def overrides: List[ModuleOverride] = List()

    def display(overrides: List[ModuleOverride] = List()): Option[Binding[Node]] = {
      overriddenBy(overrides) match {
        case Some(module) ⇒ module.display(overrides)
        case _ ⇒ if (doDisplay) Option(displaySelf(overrides)) else None
      }
    }

    protected def displaySelf(overrides: List[ModuleOverride]): Binding[Node]

    private def overriddenBy(overrides: List[ModuleOverride]): Option[DisplayModule[_]] =
      overrides.collectFirst {
        case ModuleOverride(by, con) if con(this) ⇒ by
      }
  }

  case class ModuleOverride(by: DisplayModule[Octopus], condition: (DisplayModule[Octopus]) ⇒ Boolean)

  class NoDisplay(override val withinOctopus: Octopus) extends DisplayModule[Octopus] {
    override def doDisplay: Boolean = false
    override protected def displaySelf(overrides: List[ModuleOverride]): Binding[Node] = null
  }

  class ModularDisplay(override val withinOctopus: Octopus) extends DisplayModule[Octopus] {
    @dom
    override protected def displaySelf(overrides: List[ModuleOverride]): Binding[Node] = {
      println("mod disp", overrides)
      val bindings: List[Binding[Node]] = getSiblingModules(this) map { m: DisplayModule[Octopus] ⇒
        m.display(overrides)
      } flatten;

      <div>
        {for (b <- bindings) yield b.bind}
      </div>
    }
  }

  trait TitleDisplay extends DisplayModule[Octopus]

  class ChildDisplay(override val withinOctopus: Octopus) extends DisplayModule[Octopus] {
    @dom
    override protected def displaySelf(overrides: List[ModuleOverride]): Binding[Node] = {
      println("child overrieds", childOverrides)
      val bindings = withinOctopus.connections.collect { case Child(c: Octopus) ⇒
        DisplayModuleDefinitions.display(c, overrides ::: childOverrides)
      }
      <div>
        {for (b <- bindings) yield b.bind}
      </div>
    }
    private def childOverrides = getSiblingModules(this) flatMap (_.overrides)
  }

  class TitleWithNav(override val withinOctopus: Space) extends DisplayModule[Space] with TitleDisplay {
    override def overrides: List[ModuleOverride] = super.overrides ::: List(
      ModuleOverride(new NoDisplay(withinOctopus), (m) ⇒ m.isInstanceOf[TitleWithNav]))

    @dom
    override def displaySelf(overrides: List[ModuleOverride]): Binding[Node] = {
      println("octopus titlewithnav", withinOctopus, overrides)
      <div class="nav-bar">
        <div>back</div>
        <div class="title">
          {Var(withinOctopus.title).bind}
        </div>
      </div>
    }
  }

  class TitleWithInput(override val withinOctopus: Space) extends DisplayModule[Space] with TitleDisplay {
    @dom
    override def displaySelf(overrides: List[ModuleOverride]): Binding[Node] = {
      <div class="title-with-input">
        <div class="title">
          {Var(withinOctopus.title).bind}
        </div>
        <input type="text"/>
      </div>
    }
  }
}