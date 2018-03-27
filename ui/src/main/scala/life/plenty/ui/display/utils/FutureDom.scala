package life.plenty.ui.display.utils

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.hub.definition.Hub
import life.plenty.ui.model.{DisplayModel, SimpleDisplayModuleDirectory}
import org.scalajs.dom.Node
import scalaz.std.list._
import scalaz.std.option._

import scala.concurrent.Future

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


  def dirDom[T](fv: Future[Option[T]], directory: SimpleDisplayModuleDirectory[T]): Binding[Node] =
    dirDom(new FutureOptVar(fv), directory)

  @dom
  def dirDom[T](fv: FutureOptVar[T], directory: SimpleDisplayModuleDirectory[T]): Binding[Node] = {
    fv.v.bind match {
      case Some(v) ⇒ directory.get(v) match {
        case Some(module) ⇒ module.html(v).bind
        case None ⇒ <span>can't display</span>
      }
      case _ ⇒ <span></span>
    }
  }


}
