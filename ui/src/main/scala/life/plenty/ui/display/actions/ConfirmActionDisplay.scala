package life.plenty.ui.display.actions

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.actions.ActionAddConfirmedMarker
import life.plenty.model.hub.Space
import life.plenty.model.utils.GraphUtils; import life.plenty.model.utils.DeprecatedGraphExtractors
import life.plenty.ui.model.DisplayModel.ActionDisplay
import org.scalajs.dom.Event
import org.scalajs.dom.raw.Node
import rx.Obs

class ConfirmActionDisplay(override val hub: Space) extends ActionDisplay[Space] {

  private lazy val actionConfirm = hub.getTopModule({ case a: ActionAddConfirmedMarker ⇒ a })
  private lazy val isConfirmed = DeprecatedGraphExtractors.markedConfirmed(hub)
  private var obs: Obs = null

  override def update(): Unit = {
    isEmpty.value_=(actionConfirm.isEmpty)
    if (obs == null) {
      obs = isConfirmed.foreach(is ⇒ {
        println(s"isconfirmed changed ${is}")
        active.value_=(is)
      })
    }
  }

  @dom
  def inactiveDisplay: Binding[Node] =
    <button type="button" class="btn btn-outline-dark symbolic btn-sm confirm-control" onclick={(e: Event) =>
      actionConfirm.get.confirm()}>
      <span class="oi oi-task" title="confirm" data:aria-hidden="true"></span>
    </button>

  @dom
  def activeDisplay: Binding[Node] = <div class="d-inline-flex">
    <button type="button" class="btn btn-sm symbolic confirm-control" onclick={(e: Event) =>
      actionConfirm.get.deconfirm()}>
      <span class="oi oi-task" title="de-confirm" data:aria-hidden="true"></span>
    </button>
  </div>
}
