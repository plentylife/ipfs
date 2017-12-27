package life.plenty.ui.model

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model._
import org.scalajs.dom.raw.Node

object DisplayModuleDefinitions {

  def display(o: Octopus): Binding[Node] = o.modules.collectFirst { case dm: DisplayModule[_] ⇒ dm.display()
  } getOrElse {noDisplay}

  @dom
  private def noDisplay: Binding[Node] = <div>This octopus has no display</div>

  trait DisplayModule[+T <: Octopus] extends Module[T] {
    def display(overrides: List[ModuleOverride] = List()): Binding[Node] = {
      overriddenBy(overrides) match {
        case Some(module) ⇒ module.display(overrides)
        case _ ⇒ displaySelf(overrides)
      }
    }

    protected def displaySelf(overrides: List[ModuleOverride]): Binding[Node]

    private def overriddenBy(overrides: List[ModuleOverride]): Option[DisplayModule[_]] =
      overrides.collectFirst {
        case ModuleOverride(by, cl) if cl == this.getClass ⇒ by
      }
  }

  //    case class ModuleOverride[T <: DisplayModule[Octopus]](by: DisplayModule[Octopus], what: Class[T])
  case class ModuleOverride(by: DisplayModule[Octopus], what: Class[_])

  class TitleWithNav(override val withinOctopus: Space) extends DisplayModule[Space] {
    @dom
    override def displaySelf(overrides: List[ModuleOverride]): Binding[Node] = {
      <div class="nav-bar">
        <div>back</div>
        <div class="title">
          {Var(withinOctopus.title).bind}
        </div>
      </div>
    }
  }
}