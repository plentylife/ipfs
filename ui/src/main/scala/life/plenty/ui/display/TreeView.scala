package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.connection.DataHub
import life.plenty.model.hub.definition.Hub
import life.plenty.ui.display.utils.Helpers.ListBindable
import life.plenty.ui.model.DisplayModule
import life.plenty.ui.model.{DisplayModel, ModuleOverride}
import org.scalajs.dom.{Event, Node}
import scalaz.std.list._
import scalaz.std.option._

trait Controller {
  def hub: Hub
  def cssClasses: Var[String]
  def content: Binding[Node]
  def onClick(e: Event): Unit
}

object TreeView {

  @dom
  def apply(controller: Controller, filter: PartialFunction[DataHub[_], Option[Controller]],
            emptyMessage: Option[String] = None): Binding[Node] = {
    implicit val ctx = controller.hub.ctx

    val hubs: ListBindable[Controller] = controller.hub.rx.getAll(filter).map(_.flatten)

    println(s"TreeView applying hub ${controller.hub} with underlying ${hubs().value} " +
      s"from ${controller.hub.rx.cons.now}")

    <span>
      {if (hubs().bind.nonEmpty) {
        val ds = hubs().map(c ⇒ display(c, filter))
        <ul class="tree-list">
          {for (d <- ds) yield d.bind}
        </ul>
      } else {
      emptyMessage map {m => <span>{m}</span>} getOrElse DisplayModel.nospan.bind
    }}
    </span>
  }

  @dom
  private def display(controller: Controller, filter: PartialFunction[DataHub[_], Option[Controller]]):
  Binding[Node] = {
    <li class={controller.cssClasses.bind} onclick={controller.onClick _}>
      {controller.content.bind}
      {TreeView(controller, filter).bind}
    </li>
  }
}
