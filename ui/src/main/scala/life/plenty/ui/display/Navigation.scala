package life.plenty.ui.display

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.hub.Space
import life.plenty.ui.display.meta.NoDisplay
import life.plenty.ui.model.DisplayModule
import life.plenty.ui.model._
import org.scalajs.dom.raw.Node

import scalaz.std.list._

class ViewStateLinks(override val hub: Space) extends DisplayModule[Space] {

  import ViewState._

  private val viewStates = List(DISCUSSION, RATING)

  override def update(): Unit = Unit

  override def overrides: List[ModuleOverride] =
    SimpleModuleOverride(this, new NoDisplay(hub), _.isInstanceOf[ViewStateLinks]) :: super.overrides

  @dom
  override protected def generateHtml(): Binding[Node] = {
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