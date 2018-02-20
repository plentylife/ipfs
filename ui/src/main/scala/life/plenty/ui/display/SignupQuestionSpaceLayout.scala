package life.plenty.ui.display

import com.thoughtworks.binding.Binding.{BindingSeq, Vars}
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi._
import life.plenty.model.octopi.definition.Hub
import life.plenty.model.utils.GraphUtils
import life.plenty.ui.display.actions.{SignupButton, SpaceActionsBar}
import life.plenty.ui.display.meta.LayoutModule
import life.plenty.ui.model.utils.Helpers._
import life.plenty.ui.model.{DisplayModel, ModuleOverride, UiContext}
import org.scalajs.dom.raw.Node

import scalaz.std.list._
import scalaz.std.option._

class SignupQuestionSpaceLayout(override val withinOctopus: Space) extends TopSpaceLayout(withinOctopus) {
  private lazy val signup = new BindableModule(withinOctopus.getTopModule({case m: SignupButton ⇒ m}), this)

  @dom
  private def ifEmpty: Binding[Node] = <span class="mt-2">:( no one has signed up yet to help</span>

  override protected def sectionsExtension(implicit overrides: List[ModuleOverride]): List[Binding[Node]] = List(
    {displayHubs(getContributions(children), "contributions section", signup.dom, ifEmpty)}
  )
}

