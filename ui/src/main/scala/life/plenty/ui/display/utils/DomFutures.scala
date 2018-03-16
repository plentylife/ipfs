package life.plenty.ui.display.utils

import java.util.Date
import scala.concurrent.ExecutionContext.Implicits.global
import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.ui.model.DisplayModel
import org.scalajs.dom.Node

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.timers.SetTimeoutHandle

class DomFutureOpt[T](f: Future[Option[T]]) {
  val v = Var[Option[T]](None)
  f foreach {v.value_=}
}

object DomBinders {
  @dom
  def text(f: Future[Option[String]]): Binding[Node] = new DomFutureOpt(f).v.bind match {
    case Some(t) ⇒ <span>t</span>
    case _ ⇒ DisplayModel.nospan.bind
  }
}

//class UpdateWithTimeout(updater: Future[timeoutDuration: Int) {
//
//  var lastUpdate = new Date().getTime
//  var timeout: SetTimeoutHandle = null
//
//  list foreach {l ⇒
//    lastUpdate = new Date().getTime
//    if (timeout != null) js.timers.clearTimeout(timeout)
//    timeout = js.timers.setTimeout(timeoutDuration)({
//      //      if (new Date().getTime - lastUpdate > 2000) {
//      println(s"BINDING ${l}")
//      val bindings = l map {e ⇒ e → render(e)} collect {case(e, Some(r)) ⇒ e -> r}
//      inner.value.clear()
//      inner.value.insertAll(0, bindings)
//      //      }
//    })
//
//  }
//}
