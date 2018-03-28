package life.plenty.ui.model

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.ui.display.utils.Helpers._
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import org.scalajs.dom.Node
import org.scalajs.dom.raw.HTMLElement

trait SimpleDisplayModule[T] {
  def html(what: T): Binding[Node]

  def htmlOpt(what: Any): Option[Binding[Node]] =
    if (fits(what)) Option(html(what.asInstanceOf[T])) else None

  def fits(what: Any): Boolean

  @dom
  def html(what: T, changed: Observable[T]): Binding[Node] = {
    val node = html(what).bind

    changed.collect({ case c if c == what ⇒ true }).foreach(_ ⇒ {
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
        node.asInstanceOf[HTMLElement].style.opacity = opacity.toString

        if (opacity >= startingOpacity) {
          org.scalajs.dom.window.clearInterval(interval)
        }
      }, 100)
    })

    node
  }
}


trait SimpleDisplayModuleDirectory[T] {
  val directory: List[SimpleDisplayModule[_]]

  def get[T](what: T): Option[SimpleDisplayModule[T]] =
    directory find { m ⇒ m.fits(what) } map {_.asInstanceOf[SimpleDisplayModule[T]]}

  def getAll[T](what: T): List[SimpleDisplayModule[T]] =
    directory filter { m ⇒ m.fits(what) } map {_.asInstanceOf[SimpleDisplayModule[T]]}

  def display[T](what: T): Option[Binding[Node]] = get(what) map {_.html(what)}
}
