package life.plenty.ui.display.utils

import java.util.Date

import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding.{BindingSeq, Vars}
import life.plenty.model.hub.Space
import life.plenty.ui.display.feed.FeedModuleDirectory
import life.plenty.ui.model.{SimpleDisplayModule, SimpleDisplayModuleDirectory}
import org.scalajs.dom.Node
import rx.{Ctx, Obs, Rx}

import scala.scalajs.js
import scala.scalajs.js.timers.SetTimeoutHandle

abstract class DomList[T](list: Rx[List[T]]) {
  private val inner = Vars[(T, Binding[Node])]()
//  private var cache = Map[T, Binding[Node]]()

  implicit val ctx: Ctx.Owner = Ctx.Owner.safe()

  private def render(what: T): Option[Binding[Node]] = getRenderer(what) map {_.html(what)}

//    synchronized {
//    cache get what match {
//      case b @ Some(_) ⇒
//        b
//      case _ ⇒ val r = getRenderer(what) map {m ⇒ m.html(what)}
//        r foreach {r ⇒ cache += (what → r)}
//        r
//    }
//  }

  //dir: SimpleDisplayModuleDirectory[_]
  protected def getRenderer(what: T): Option[SimpleDisplayModule[T]]

  var lastUpdate = new Date().getTime
  var timeout: SetTimeoutHandle = null

  list foreach {l ⇒
    lastUpdate = new Date().getTime
    if (timeout != null) js.timers.clearTimeout(timeout)
    timeout = js.timers.setTimeout(3000)({
//      if (new Date().getTime - lastUpdate > 2000) {
      println(s"BINDING ${l}")
        val bindings = l map {e ⇒ e → render(e)} collect {case(e, Some(r)) ⇒ e -> r}
        inner.value.clear()
        inner.value.insertAll(0, bindings)
//      }
    })

  }

  def apply(): BindingSeq[Binding[Node]] = inner.map(_._2)
}

class DomListSingleModule[T](list: Rx[List[T]], simpleDisplayModule: SimpleDisplayModule[T]) extends DomList[T](list) {
  override protected def getRenderer(what: T): Option[SimpleDisplayModule[T]] = Option(simpleDisplayModule)
}
