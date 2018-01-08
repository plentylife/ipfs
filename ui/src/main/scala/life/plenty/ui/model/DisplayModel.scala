package life.plenty.ui.model

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model._
import org.scalajs.dom.raw.Node

import scala.language.postfixOps
import scalaz.std.list._

object DisplayModel {

  def display(o: Octopus, overrides: List[ModuleOverride] = List()): Binding[Node] = {
    o.modules.collectFirst({ case dm: DisplayModule[_] ⇒ dm.display(overrides)
    }).flatten getOrElse noDisplay
  }

  def reRender(o: Octopus): Unit = o.getTopModule({ case m: DisplayModule[_] ⇒ m })
    .flatMap(_.displayWithMemory())

  def getSiblingModules(self: DisplayModule[Octopus]): List[DisplayModule[Octopus]] = self.withinOctopus.getModules {
    case m: DisplayModule[_] if m != self ⇒ m }

  @dom
  private def noDisplay: Binding[Node] = <div>This octopus has no display</div>

  trait DisplayModule[+T <: Octopus] extends Module[T] {
    private var existingBinding: Option[Var[Node]] = None
    private var onLastDisplayHadOverrides = List[ModuleOverride]()

    def doDisplay(): Boolean = true

    def overrides: List[ModuleOverride] = List()

    def display(overrides: List[ModuleOverride] = List()): Option[Binding[Node]] = {
      //      println("displaying ", this, withinOctopus)
      onLastDisplayHadOverrides = overrides
      overriddenBy(overrides) match {
        case Some(module) ⇒ module.display(overrides)
        case _ ⇒ if (doDisplay()) {
          Option(innerDisplay(overrides))
        } else None
      }
    }

    def displayWithMemory(): Option[Binding[Node]] = {
      //      println("displaying with memory ", withinOctopus)
      display(onLastDisplayHadOverrides)
    }

    protected def generateHtml(overrides: List[ModuleOverride]): Binding[Node]

    @dom
    private def innerDisplay(overrides: List[ModuleOverride]): Binding[Node] = existingBinding match {
      case Some(v) ⇒
        println("inner old var", this, this.withinOctopus)
        v.value_=(<div>test</div>)
        //        v.value_=(generateHtml(overrides).bind)
        v.bind
      case None ⇒
        println("inner new var", this, this.withinOctopus)
        val v = Var(generateHtml(overrides).bind)
        existingBinding = Option(v)
        //        v.value_=(<div>test</div>)
        v.bind
    }

    private def overriddenBy(overrides: List[ModuleOverride]): Option[DisplayModule[_]] =
      overrides.collectFirst {
        case ModuleOverride(by, con) if con(this) ⇒ by
      }
  }

  case class ModuleOverride(by: DisplayModule[Octopus], condition: (DisplayModule[Octopus]) ⇒ Boolean)

  trait TitleDisplay extends DisplayModule[Octopus]
}