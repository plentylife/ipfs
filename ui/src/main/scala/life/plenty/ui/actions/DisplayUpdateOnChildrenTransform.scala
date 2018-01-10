package life.plenty.ui.actions

import life.plenty.model._
import life.plenty.model.actions.ActionAfterGraphTransform
import life.plenty.model.connection.{Child, Connection}
import life.plenty.ui.model.DisplayModel

class DisplayUpdateOnChildrenTransform(override val withinOctopus: Octopus) extends ActionAfterGraphTransform {
  override def onConnectionAdd(connection: Connection[_]): Either[Exception, Unit] = {
    //    println("re-render listener triggered", withinOctopus, connection)
    connection match {
      case Child(_) ⇒
        println("re-render listener executed", withinOctopus, connection)
        // todo fixme this funcion is flawed
        DisplayModel.reRender(withinOctopus)
        Right()
      case _ ⇒
        //        println("no")
        Right()
    }
    Right()
  }

  override def onConnectionRemove(connection: Connection[_]): Either[Exception, Unit] = ???
}
