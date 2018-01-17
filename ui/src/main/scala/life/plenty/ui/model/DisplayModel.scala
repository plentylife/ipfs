package life.plenty.ui.model

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.{Module, Octopus}
import org.scalajs.dom.raw.Node

import scala.language.postfixOps
import scalaz.std.list._

object DisplayModel {

  def display(o: Octopus, overrides: List[ModuleOverride] = List()): Binding[Node] = {
    o.modules.collectFirst({ case dm: DisplayModule[_] ⇒
      dm.display(None, overrides)
    }).flatten getOrElse noDisplay
  }

  @dom
  private def noDisplay: Binding[Node] = <div>This octopus has no display</div>

  def reRender(o: Octopus, moduleSelector: PartialFunction[Module[Octopus], DisplayModule[Octopus]] = {
    case m: DisplayModule[_] ⇒ m
  }): Unit =
    o.getTopModule(moduleSelector).foreach(m ⇒ {
      if (m.hasRendered) {
        println("re-render of module", m)
        m.update()
      }
    })
  def getSiblingModules(self: DisplayModule[Octopus]): List[DisplayModule[Octopus]] = self.withinOctopus.getModules {
    case m: DisplayModule[_] if m != self ⇒ m
  }

  /* the main trait */

  trait DisplayModule[+T <: Octopus] extends Module[T] {
    private var _hasRenderedOnce = false

    def display(calledBy: DisplayModule[Octopus], overrides: List[ModuleOverride] = List()): Option[Binding[Node]] = {
      this.display(Option(calledBy), overrides)
    }

    def doDisplay(): Boolean = true

    def overrides: List[ModuleOverride] = List()

    def display(calledBy: Option[DisplayModule[_]], overrides: List[ModuleOverride]): Option[Binding[Node]] = {
      overriddenBy(overrides) match {
        case Some(module) ⇒ module.display(calledBy, overrides)
        case _ ⇒ if (doDisplay()) {
          //          println("displaying ", this, withinOctopus, calledBy)
          update()
          val html = Option(generateHtml(overrides))
          if (!_hasRenderedOnce) _hasRenderedOnce = true
          html
        } else None
      }
    }

    def update(): Unit

    def hasRendered = _hasRenderedOnce

    //    def orderPreference(toReorder: List[Octopus]) = identity(toReorder)

    protected def generateHtml(overrides: List[ModuleOverride]): Binding[Node]

    private def overriddenBy(overrides: List[ModuleOverride]): Option[DisplayModule[_]] =
      overrides.collectFirst {
        case ModuleOverride(creator, by, condition) if creator != this && condition(this) ⇒ by
      }

  }

  case class ModuleOverride(creator: DisplayModule[Octopus], by: DisplayModule[Octopus], condition:
  (DisplayModule[Octopus]) ⇒ Boolean)

}