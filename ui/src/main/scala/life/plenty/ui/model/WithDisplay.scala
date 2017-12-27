package life.plenty.ui.model

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model._
import life.plenty.ui.model.DisplayModuleDefinitions.{DisplayModule, TitleWithNav}
import org.scalajs.dom.raw.Node

object DisplayModuleDefinitions {

  trait DisplayModule[+T] extends Module[T] {
    def display(overrides: List[ModuleOverride] = List()): Binding[Node] = {
      overriddenBy(overrides) match {
        case Some(module) ⇒ module.display(overrides)
        case _ ⇒ displaySelf(overrides)
      }
    }

    protected def displaySelf(overrides: List[ModuleOverride]): Binding[Node]

    private def overriddenBy(overrides: List[ModuleOverride]): Option[DisplayModule[_]] =
      overrides.collectFirst {
        case ModuleOverride(by, this.getClass) ⇒ by
      }
  }

  //  class ModuleOverride[T <: DisplayModule](by: DisplayModule, what: Class[T])
  case class ModuleOverride(by: DisplayModule[_], what: Class[_])

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

trait DisplayWrapper[+T <: Octopus] {
  val wraps: T
  val displayModule: DisplayModule[T]

  def display = displayModule.display()

  wraps.addModule(displayModule)
}


class SpaceWrapper(override val wraps: Space) extends DisplayWrapper[Space] {
  override val displayModule = new TitleWithNav(wraps)
}
