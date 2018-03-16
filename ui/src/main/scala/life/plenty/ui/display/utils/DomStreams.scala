package life.plenty.ui.display.utils

import com.thoughtworks.binding.Binding.Vars
import life.plenty.model.connection.DataHub
import life.plenty.model.hub.definition.Hub
import monix.execution.Scheduler.Implicits.global

class DomStream[T](hub: Hub, extractor: PartialFunction[DataHub[_], T]) {
  val v = Vars[T]()

  hub.conExList(extractor) foreach {list ⇒

    hub.inserts.collect(extractor).foreach(dh ⇒ v.value.insert(0, dh))
    hub.removes.collect(extractor).foreach(dh ⇒ v.value.remove(v.value.indexOf(dh)))
  }

}
