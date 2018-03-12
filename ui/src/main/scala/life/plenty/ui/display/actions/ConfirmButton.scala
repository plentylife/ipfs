package life.plenty.ui.display.actions

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.actions.{ActionAddConfirmedMarker, ActionGiveThanks}
import life.plenty.model.connection.{Marker, MarkerEnum}
import life.plenty.model.octopi.{Contribution, Space}
import life.plenty.model.utils.GraphUtils
import life.plenty.ui
import life.plenty.ui.display.actions.labeltraits.MenuAction
import life.plenty.ui.display.utils.ModalFormAction
import life.plenty.ui.model.DisplayModel.ActionDisplay
import life.plenty.ui.model.UiContext
import life.plenty.ui.display.utils.InputVarWithDisplay
import org.scalajs.dom.{Event, Node}
import rx.Obs

class ConfirmButton(override val hub: Space) extends ActionDisplay[Space] with MenuAction {

  private lazy val module: Option[ActionAddConfirmedMarker] = hub
    .getTopModule({ case m: ActionAddConfirmedMarker => m })
  private lazy val isConfirmed = GraphUtils.markedConfirmed(hub)
  private var obs: Obs = null

  override def update(): Unit = {
    if (obs == null) {
      obs = isConfirmed.foreach(active.value_=)
    }
    isEmpty.value_=(module.isEmpty)
  }

  @dom
  override def activeDisplay: Binding[Node] = <div class="btn btn-outline-primary"
                                                   onclick={e: Event ⇒ module.get.deconfirm()}>
    unmark final
  </div>

  @dom
  override def inactiveDisplay: Binding[Node] = <div class="btn btn-primary"
                                                     onclick={e: Event ⇒ module.get.confirm()}>
    mark final
  </div>
}