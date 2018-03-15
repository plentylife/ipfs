package life.plenty.ui.display.actions

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.actions.ActionRemove
import life.plenty.model.hub.Space
import life.plenty.ui.model.DisplayModel.ActionDisplay
import org.scalajs.dom.Event
import org.scalajs.dom.raw.Node

class EditSpace(override val hub: Space) extends ActionDisplay[Space] {
  override def update(): Unit = {
    isEmpty.value_=(actionRemove.isEmpty)
  }

  @dom
  def inactiveDisplay: Binding[Node] =
    <button type="button" class="btn btn-outline-dark symbolic btn-sm" onclick={(e: Event) => active.value_=(true)}>
      <span class="oi oi-pencil" title="edit" data:aria-hidden="true"></span>
    </button>

  @dom
  def activeDisplay: Binding[Node] = <div class="d-inline-flex">
    {ChangeParent.displayInactiveOnly(hub, Option(close _)).bind}
    <button type="button" class="btn btn-outline-danger btn-sm symbolic" onclick={(e: Event) =>
      active.value_=(false); actionRemove.get.remove()}>
      <span class="oi oi-trash" title="remove" data:aria-hidden="true"></span>
    </button>
  </div>

  private def close() = {
    active.value_=(false)
  }

  private lazy val actionRemove = {
    val a = hub.getTopModule({ case a: ActionRemove ⇒ a })
    a
  }
}
