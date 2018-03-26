package life.plenty.ui.model

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.hub.definition.Hub
import life.plenty.ui.display.utils.Helpers._
import monix.reactive.Observable
import org.scalajs.dom.{Node, StyleSheet}
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.{HTMLAnchorElement, HTMLElement}
import rx.{Ctx, Rx, Var ⇒ rxVar}

import scala.scalajs.js
import scala.scalajs.js.timers.SetIntervalHandle

trait SimpleDisplayModule[T] {
  def html(what: T): Binding[Node]
  def htmlOpt(what: Any): Option[Binding[Node]] =
    if (fits(what)) Option(html(what.asInstanceOf[T])) else None
  def fits(what: Any): Boolean

  @dom
  def html(what: T, changed: Observable[T]): Binding[Node] = {
    val node = html(what).bind

    changed.collect({case c if c == what ⇒ true}).foreach(_ ⇒ {
      val startingOpacity = {
        val op = node.asInstanceOf[HTMLElement].style.opacity
          if (op.isEmpty) 1.0 else op.toDouble
      }
      node.asInstanceOf[HTMLElement].style.opacity = 0
      var opacity = 0.0
      var interval: Int = -1
      interval = org.scalajs.dom.window.setInterval(() ⇒ {
        opacity += 0.2
        if (opacity > startingOpacity) opacity = startingOpacity
        println(s"Opacity $opacity")
        node.asInstanceOf[HTMLElement].style.opacity = opacity.toString

        if (opacity >= startingOpacity) {
          println("Opacity cancel")
          org.scalajs.dom.window.clearInterval(interval)
        }
      }, 100)
    })

    node
  }
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

//  def html[T <: Hub](mh: (SimpleDisplayModule[T], Rx[Option[T]])): Binding[Node] = html(mh._1, mh._2)
}

trait SimpleDisplayModuleDirectory[L <: SimpleDisplayModule[_]] {
  val directory : List[L]
  def get[T](what: T): Option[SimpleDisplayModule[T]] =
    directory find {m ⇒ m.fits(what)} map {_.asInstanceOf[SimpleDisplayModule[T]]}
  def getTogether[T](hub: T): Option[(SimpleDisplayModule[T], T)] = get(hub) map {_ → hub}
}
