package life.plenty.ui.display.actions

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.actions.ActionAddConfirmedMarker
import life.plenty.model.octopi.Space
import life.plenty.model.utils.ConFinders
import life.plenty.ui.model.DisplayModel.ActionDisplay
import org.scalajs.dom.Event
import org.scalajs.dom.raw.Node
import rx.Obs

class ConfirmActionDisplay(override val withinOctopus: Space) extends ActionDisplay[Space] {

  private lazy val actionConfirm = withinOctopus.getTopModule({ case a: ActionAddConfirmedMarker ⇒ a })
  private lazy val isConfirmed = ConFinders.markedConfirmed(withinOctopus)
  private lazy val obs: Obs = null

  override def update(): Unit = {
    isEmpty.value_=(actionConfirm.isEmpty)
    if (obs == null) {
      isConfirmed.foreach(active.value_=)
    }
  }

  @dom
  def inactiveDisplay: Binding[Node] =
    <button type="button" class="btn btn-outline-dark symbolic btn-sm confirm-control" onclick={(e: Event) =>
      actionConfirm.get.confirm()}>
      <span class="oi oi-task" title="edit" data:aria-hidden="true"></span>
    </button>

  @dom
  def activeDisplay: Binding[Node] = <div class="d-inline-flex">
    {ChangeParent.displayInactiveOnly(withinOctopus, Option(close _)).bind}<button type="button" class="btn
    btn-outline-danger btn-sm symbolic confirm-control" onclick={(e: Event) => ???}>
      <span class="oi oi-task" title="remove" data:aria-hidden="true"></span>
    </button>
  </div>

  private def close() = {
    active.value_=(false)
  }
}
