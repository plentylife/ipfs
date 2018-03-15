package life.plenty.ui.model

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.hub.definition.Hub
import life.plenty.ui.display.utils.Helpers._
import org.scalajs.dom.Node
import rx.{Ctx, Rx, Var ⇒ rxVar}

trait SimpleDisplayModule[T] {
  def html(what: T): Binding[Node]
  def fits(what: Any): Boolean
}

object SimpleDisplayModule {
  @dom
  def html[T](module: SimpleDisplayModule[T], hub: Rx[Option[T]]): Binding[Node] = {
    val hb: BasicBindable[Option[T]] = hub
    hb().bind match {
      case Some(h) ⇒ module.html(h).bind
      case None ⇒ DisplayModel.nospan.bind
    }
  }

  def html[T <: Hub](mh: (SimpleDisplayModule[T], Rx[Option[T]])): Binding[Node] = html(mh._1, mh._2)
}

trait SimpleDisplayModuleDirectory[L <: SimpleDisplayModule[_]] {
  val directory : List[L]
  def get[T](what: T): Option[SimpleDisplayModule[T]] =
    directory find {m ⇒ m.fits(what)} map {_.asInstanceOf[SimpleDisplayModule[T]]}
  def getTogether[T <: Hub](hub: T): Option[(SimpleDisplayModule[T], T)] = get(hub) map {_ → hub}
}
