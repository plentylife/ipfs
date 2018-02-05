package life.plenty.ui.display.actions

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.actions.ActionRemove
import life.plenty.model.octopi.Space
import life.plenty.ui.model.DisplayModel.ActionDisplay
import org.scalajs.dom.Event
import org.scalajs.dom.raw.Node

class EditSpace(override val withinOctopus: Space) extends ActionDisplay[Space] {
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
    <button type="button" class="btn btn-outline-danger btn-sm symbolic" onclick={(e: Event) =>
      active.value_=(false); actionRemove.get.remove()}>
      <span class="oi oi-trash" title="remove" data:aria-hidden="true"></span>
    </button>{ChangeParent.displayInactiveOnly(withinOctopus, Option(close _)).bind}
  </div>

  private def close() = {
    active.value_=(false)
  }

  private lazy val actionRemove = {
    val a = withinOctopus.getTopModule({ case a: ActionRemove ⇒ a })
    a
  }
}
