package life.plenty.ui.display.utils

import com.thoughtworks.binding.Binding.Vars
import life.plenty.model.connection.DataHub
import life.plenty.model.hub.definition.{Hub, ObsStream}
import monix.execution.Scheduler.Implicits.global

class DomStream[T](hub: Hub, extractor: PartialFunction[DataHub[_], T]) {
  val v = Vars[T]()

  hub.conExList(extractor) foreach { list ⇒
    v.value.insertAll(0, list)
    hub.inserts.collect(extractor).foreach(dh ⇒ v.value.insert(0, dh))
    hub.removes.collect(extractor).foreach(dh ⇒ v.value.remove(v.value.indexOf(dh)))
  }

}

class DomStreamObs[T](obsStream: ObsStream[T]) {
  val v = Vars[T]()

  obsStream._inserts.foreach(dh ⇒ v.value.insert(0, dh))
  obsStream._removes.foreach(dh ⇒ v.value.remove(v.value.indexOf(dh)))

}
