package life.plenty.ui.display.utils

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.ui.model.DisplayModel
import org.scalajs.dom.Node

import scala.concurrent.Future

object DomBinders {
  @dom
  def text(f: Future[Option[String]]): Binding[String] = new DomFutureOpt(f).v.bind match {
    case Some(t) ⇒ t
    case _ ⇒ ""
  }

//
//  @dom
//  def coverEmpty
}