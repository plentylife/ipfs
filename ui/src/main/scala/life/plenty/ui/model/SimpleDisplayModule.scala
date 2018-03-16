package life.plenty.ui.model

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.hub.definition.Hub
import life.plenty.ui.display.utils.Helpers._
import org.scalajs.dom.Node
import rx.{Ctx, Rx, Var ⇒ rxVar}

trait SimpleDisplayModule[T] {
  implicit val selfImplicit: SimpleDisplayModule[T] = this
  private var cache: Map[(T, Any), Binding[Node]] = Map()

  protected def htmlGen(what: T): Binding[Node]
  def html(what: T)(implicit caller: SimpleDisplayModule[_]): Binding[Node] = {
    val c = cache.get(what → caller)
    c getOrElse {
      val gen = htmlGen(what)
      cache += (what → caller) → gen
      gen
    }
  }
  def htmlOpt(what: Any)(implicit caller: SimpleDisplayModule[_]) : Option[Binding[Node]] =
    if (fits(what)) Option(html(what.asInstanceOf[T])) else None
  def fits(what: Any): Boolean
}

// fixme caller should be a simpledisplaymodule

object SimpleDisplayModule {
  @dom
  def html[T](module: SimpleDisplayModule[T], hub: Rx[Option[T]])(implicit caller: SimpleDisplayModule[_]): Binding[Node] = {
    val hb: BasicBindable[Option[T]] = hub
    hb().bind match {
      case Some(h) ⇒ module.html(h).bind
      case None ⇒ DisplayModel.nospan.bind
    }
  }

//  def html[T <: Hub](mh: (SimpleDisplayModule[T], Rx[Option[T]])): Binding[Node] = html(mh._1, mh._2)
}

trait SimpleDisplayModuleDirectory[L <: SimpleDisplayModule[_]] {
  val directory : List[L]
  def get[T](what: T): Option[SimpleDisplayModule[T]] =
    directory find {m ⇒ m.fits(what)} map {_.asInstanceOf[SimpleDisplayModule[T]]}
  def getTogether[T](hub: T): Option[(SimpleDisplayModule[T], T)] = get(hub) map {_ → hub}
}
