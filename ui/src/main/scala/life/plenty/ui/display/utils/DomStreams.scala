package life.plenty.ui.display.utils

import com.thoughtworks.binding.Binding.{Var, Vars}
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.connection.DataHub
import life.plenty.model.hub.definition.{GraphOp, Hub, Insert, Remove}
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import org.scalajs.dom.Node
//
//class DomStream[T](hub: Hub, extractor: PartialFunction[DataHub[_], T]) {
//  val v = Vars[T]()
//
//  hub.conExList(extractor) foreach { list ⇒
//    v.value.insertAll(0, list)
//    hub.feed.collect(extractor).foreach(dh ⇒ v.value.insert(0, dh))
//    hub.removes.collect(extractor).foreach(dh ⇒ v.value.remove(v.value.indexOf(dh)))
//  }
//
//}

class DomHubStream(stream: Observable[Hub]) {
  val v = Vars[Hub]()
  stream.foreach(h ⇒ v.value.insert(0, h))
}

class DomOpStream[T](stream: Observable[GraphOp[T]]) {
  val v = Vars[T]()
  stream.foreach {
    case Insert(what) ⇒ v.value.insert(0, what)
    case Remove(what) ⇒ val current = v.value
      val index = current.indexOf(what)
      if (index > -1) current.remove(index)
  }
}

class DomValStream[T](stream: Observable[T]) {
  val v = Var[Option[T]](None)
  stream.foreach(h ⇒ v.value_=(Option(h)))
}

object DomValStream {
  implicit class StringDom[T <% String](dv: DomValStream[T]) {
    @dom
    def dom: Binding[String] = {
      dv.v.bind match {
        case Some(value) ⇒ (value : String)
        case None ⇒ ""
      }
    }
  }


}

