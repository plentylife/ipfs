package life.plenty.ui.display.utils

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.ui.model.DisplayModel
import org.scalajs.dom.Node

object FutureDom {

  @dom
  def dom[T](fv: FutureVar[T], html: T ⇒ Binding[Node]): Binding[Node] = {
    fv.v.bind match {
      case Some(v) ⇒ html(v).bind
      case _ ⇒ DisplayModel.nospan.bind
    }
  }

}
