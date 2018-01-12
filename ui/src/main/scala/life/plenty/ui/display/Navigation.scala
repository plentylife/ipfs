package life.plenty.ui.display

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.Space
import life.plenty.ui.model.DisplayModel.{DisplayModule, ModuleOverride}
import life.plenty.ui.model.{DisplayModel, Router, ViewState}
import org.scalajs.dom.raw.Node

import scalaz.std.list._

class ViewStateLinks(override val withinOctopus: Space) extends DisplayModule[Space] {

  import ViewState._

  private val viewStates = List(DISCUSSION, RATING)

  override def update(): Unit = Unit

  override def overrides: List[DisplayModel.ModuleOverride] =
    ModuleOverride(this, new NoDisplay(withinOctopus), _.isInstanceOf[ViewStateLinks]) :: super.overrides

  @dom
  override protected def generateHtml(overrides: List[DisplayModel.ModuleOverride]): Binding[Node] = {
    <ul>
      {for (vs <- viewStates) yield displayLink(vs).bind}
    </ul>
  }

  @dom
  private def displayLink(vs: ViewState): Binding[Node] = {
    val modRoute = Router.changeViewState(vs, Router.router.state.bind)
    <li>
      <a href={Router.toHash(modRoute)}>
        {vs.toString}
      </a>
    </li>
  }
}