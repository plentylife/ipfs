package life.plenty.ui.model

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.definition.Hub
import life.plenty.ui.display.utils.Helpers._
import org.scalajs.dom.Node
import rx.{Ctx, Rx, Var ⇒ rxVar}

trait SimpleDisplayModule[T <: Hub] {
  def html(hub: T): Binding[Node]
}

object SimpleDisplayModule {
  @dom
  def html[T <: Hub](module: SimpleDisplayModule[T], hub: Rx[Option[T]]): Binding[Node] = {
    val hb: BasicBindable[Option[T]] = hub
    hb().bind match {
      case Some(h) ⇒ module.html(h).bind
      case None ⇒ DisplayModel.nospan.bind
    }
  }
}
