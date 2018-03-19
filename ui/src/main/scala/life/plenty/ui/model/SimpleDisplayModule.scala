package life.plenty.ui.model

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.hub.definition.Hub
import life.plenty.ui.display.utils.DomValStream
import life.plenty.ui.display.utils.Helpers._
import monix.reactive.Observable
import org.scalajs.dom.Node
import rx.{Ctx, Rx, Var ⇒ rxVar}

trait SimpleDisplayModule[T] {
  def html(what: T): Binding[Node]
  def htmlOpt(what: Any): Option[Binding[Node]] =
    if (fits(what)) Option(html(what.asInstanceOf[T])) else None
  def fits(what: Any): Boolean
}

object SimpleDisplayModule {
  @dom
  def html[T](module: SimpleDisplayModule[T], what: Observable[T]): Binding[Node] = {
    new DomValStream(what).v.bind match {
      case Some(value) ⇒ module.html(value).bind
      case None ⇒ DisplayModel.nospan.bind
    }
  }

}

trait SimpleDisplayModuleDirectory[L <: SimpleDisplayModule[_]] {
  val directory : List[L]
  def get[T](what: T): Option[SimpleDisplayModule[T]] =
    directory find {m ⇒ m.fits(what)} map {_.asInstanceOf[SimpleDisplayModule[T]]}
  def getTogether[T](hub: T): Option[(SimpleDisplayModule[T], T)] = get(hub) map {_ → hub}
}
