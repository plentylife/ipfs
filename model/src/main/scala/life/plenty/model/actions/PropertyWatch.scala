package life.plenty.model.actions

import life.plenty.model.connection.Connection
import life.plenty.model.octopi.{Octopus, Property}

class PropertyWatch[T](override val withinOctopus: Octopus, val property: Property[T]) extends ActionOnGraphTransform {
  override def onConnectionAdd(connection: Connection[_]): Either[Exception, Unit] = {
    //    println("running property watch", property.getClass.getSimpleName)
    if (property.getter.isDefinedAt(connection)) {
      //      println("watch update")
      property.update(connection)
    }
    Right()
  }

  override def onConnectionRemove(connection: Connection[_]): Either[Exception, Unit] = ???
}
