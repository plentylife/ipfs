package life.plenty.ui.display.actions

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.actions.{ActionAddConfirmedMarker, ActionGiveThanks}
import life.plenty.model.connection.{Marker, MarkerEnum}
import life.plenty.model.octopi.{Contribution, Space}
import life.plenty.model.utils.GraphUtils
import life.plenty.ui
import life.plenty.ui.display.utils.ModalFormAction
import life.plenty.ui.model.DisplayModel.ActionDisplay
import life.plenty.ui.model.UiContext
import life.plenty.ui.display.utils.InputVarWithDisplay
import org.scalajs.dom.{Event, Node}
import rx.Obs

trait MenuAction

class ConfirmButton(override val withinOctopus: Space) extends ActionDisplay[Space] with MenuAction {

  private lazy val module: Option[ActionAddConfirmedMarker] = withinOctopus
    .getTopModule({case m: ActionAddConfirmedMarker => m})
  private lazy val isConfirmed = GraphUtils.markedConfirmed(withinOctopus)
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
    de-confirm
  </div>

  @dom
  override def inactiveDisplay: Binding[Node] = <div class="btn btn-primary"
                                                     onclick={e: Event ⇒ module.get.confirm()}>
    confirm
  </div>
}