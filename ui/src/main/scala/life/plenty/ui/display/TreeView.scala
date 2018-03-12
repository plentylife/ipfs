package life.plenty.ui.display

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.connection.DataHub
import life.plenty.model.octopi.definition.Hub
import life.plenty.ui.display.utils.Helpers.ListBindable
import life.plenty.ui.model.DisplayModel.DisplayModule
import life.plenty.ui.model.{DisplayModel, ModuleOverride}
import org.scalajs.dom.{Event, Node}
import scalaz.std.list._
import scalaz.std.option._

trait Controller {
  def cssClasses: String
  def prependContent: Binding[Node]
  def onClick(e: Event): Unit
}

object TreeView {

  @dom
  def apply(hub: Hub, filter: PartialFunction[DataHub[_], Hub], caller: DisplayModule[Hub],
            overrides: List[ModuleOverride] = List(),
            controllerGenerator: Hub => Controller): Binding[Node] = {
    implicit val ctx = hub.ctx

    val hubs: ListBindable[Hub] = hub.rx.getAll(filter)

    println(s"TreeView applying hub ${hub} with underlying ${hubs().value} from ${hub.rx.cons.now}")

    <span>
      {DisplayModel.display(hub, overrides, caller).bind}
      {if (hubs().bind.nonEmpty) {
        val ds = hubs().map(h ⇒ display(h, filter, caller, overrides, controllerGenerator).bind)
        <ul class="tree-list">
          {for (d <- ds) yield d}
        </ul>
      } else DisplayModel.nospan.bind}
    </span>
  }

  @dom
  private def display(hub: Hub, filter: PartialFunction[DataHub[_], Hub], caller: DisplayModule[Hub],
                      overrides: List[ModuleOverride],
                      controllerGenerator: Hub => Controller): Binding[Node] = {
    val c = controllerGenerator(hub)
    <li class={c.cssClasses} onclick={c.onClick _}>
      {c.prependContent.bind}
      {TreeView(hub, filter, caller, overrides, controllerGenerator).bind}
    </li>
  }
}
