package life.plenty.ui.display.menu

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.hub.definition.Hub
import life.plenty.model.utils.GraphEx._
import life.plenty.ui.display.utils.FutureDom._
import life.plenty.ui.display.utils.FutureNakedVar
import life.plenty.ui.model.SimpleDisplayModule
import org.scalajs.dom.Node

import scala.concurrent.ExecutionContext.Implicits.global

object DeleteButton extends SimpleDisplayModule[Hub] {

  override def html(what: Hub): Binding[Node] = {
    val hasParent = new FutureNakedVar(getParent(what).map(_.nonEmpty))

    depDom(hasParent, buttonHtml)
  }

  @dom
  private def buttonHtml: Binding[Node] = {
    //onclick={e: Event ⇒ open()}
    <div class="btn btn-outline-danger">
      delete
    </div>
  }

  override def fits(what: Any): Boolean = what match {
    case hub: Hub ⇒ true
    //    case hub: Hub ⇒ hub.sc.ex({case Parent(p) ⇒ p}).nonEmpty
    case _ ⇒ false
  }
}
