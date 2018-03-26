package life.plenty.ui.display.utils

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.ui.model.DisplayModel
import org.scalajs.dom.Node
import scalaz.std.list._
import scalaz.std.option._

object FutureDom {

  @dom
  def dom[T](fv: FutureVar[T], html: T ⇒ Binding[Node]): Binding[Node] = {
    fv.v.bind match {
      case Some(v) ⇒ html(v).bind
      case _ ⇒ DisplayModel.nospan.bind
    }
  }

  @dom
  implicit def propertyDom[String](fv: FutureOptVar[String]): Binding[Node] = {
    fv.v.bind match {
      case Some(v) ⇒ <span>
        {v.toString}
      </span>
      case _ ⇒ <span></span>
    }
  }

}
