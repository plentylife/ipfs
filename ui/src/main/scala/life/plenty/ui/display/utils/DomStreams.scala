package life.plenty.ui.display.utils

import com.thoughtworks.binding.Binding.{Var, Vars}
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.connection.DataHub
import life.plenty.model.hub.definition.{GraphOp, Hub, Insert, Remove}
import life.plenty.ui.model.{DisplayModel, SimpleDisplayModule}
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import org.scalajs.dom.Node

class DomStream[T](stream: Observable[T]) {
  val v = Vars[T]()
  stream.foreach { elem =>
    val c = v.value
    if (c.indexOf(elem) < 0) v.value.insert(0, elem)
  }
}

class DomHubStream(stream: Observable[Hub]) extends DomStream[Hub](stream)

class DomOpStream[T](stream: Observable[GraphOp[T]]) {
  val v = Vars[T]()

  stream.foreach {
    case Insert(what) ⇒
      println(s"DOS $what")
      v.value.insert(0, what)
      println(s"DOS ${v.value}")
    case Remove(what) ⇒ val current = v.value
      val index = current.indexOf(what)
      if (index > -1) current.remove(index)
  }
}

class DomValStream[T](val stream: Observable[T]) {
  val v = Var[Option[T]](None)
  stream.foreach(h ⇒ v.value_=(Option(h)))

  stream.dump("VAL").subscribe()
}

object DomValStream {
  class DisplayDom[T](stream: Observable[T], module: SimpleDisplayModule[T]) {
    def dom: Binding[Node] = {
      new BindingDom(new DomValStream(stream map {module.html})).dom
    }
  }

  implicit class ImplicitStringDom[T <% String](dv: Observable[T]) {
    def dom: Binding[String] = {
      new StringDom(new DomValStream(dv)).dom
    }
  }

  implicit class StringDom[T <% String](dv: DomValStream[T]) {
    @dom
    def dom: Binding[String] = {
      dv.v.bind match {
        case Some(value) ⇒ (value : String)
        case None ⇒ ""
      }
    }
  }

  implicit class BindingDom[T <% Binding[Node]](dv: DomValStream[T]) {
    @dom
    def dom: Binding[Node] = {
      dv.v.bind match {
        case Some(value) ⇒ (value : Binding[Node]).bind
        case None ⇒ DisplayModel.nospan.bind
      }
    }
  }

  implicit class BooleanDom(dv: DomValStream[Boolean]) {
    @dom
    def dom: Binding[Boolean] = {
      dv.v.bind match {
        case Some(value) ⇒ value
        case None ⇒ false
      }
    }
  }
}

