package life.plenty.ui.display.menu

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.actions.ActionDelete
import life.plenty.model.hub.definition.Hub
import life.plenty.model.utils.GraphEx._
import life.plenty.ui.display.utils.FutureDom._
import life.plenty.ui.display.utils.FutureNakedVar
import life.plenty.ui.model.{Router, SimpleDisplayModule}
import org.scalajs.dom.Node
import org.scalajs.dom.Event

import scala.concurrent.ExecutionContext.Implicits.global

object DeleteButton extends SimpleDisplayModule[Hub] {
  // fixme should be able to undelete
  override def html(what: Hub): Binding[Node] = {
    val hasParent = new FutureNakedVar(getParent(what).map(_.nonEmpty))

    depDom(hasParent, deleteHtml(what))
  }

  @dom
  private def deleteHtml(hub: Hub): Binding[Node] = {
    val confirming = Var(false)
    //onclick={e: Event ⇒ open()}
    <div class="btn btn-outline-danger" onclick={onClick(hub, confirming) _}>
      {if (confirming.bind) "are you sure?" else "delete"}
    </div>
  }

  private def onClick(hub: Hub, confirming: Var[Boolean])(e: Event) = {
    if (confirming.value) {
      ActionDelete.delete(hub) flatMap {
        _ ⇒ getParent(hub)
      } foreach {
        _ foreach {p ⇒ Router.navigateToHub(p)}
      }
    } else {
      confirming.value_=(true)
    }
  }



  override def fits(what: Any): Boolean = what match {
    case hub: Hub ⇒ true
    //    case hub: Hub ⇒ hub.sc.ex({case Parent(p) ⇒ p}).nonEmpty
    case _ ⇒ false
  }
}
