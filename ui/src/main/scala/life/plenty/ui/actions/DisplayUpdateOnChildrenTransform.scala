package life.plenty.ui.actions

import life.plenty.model._
import life.plenty.model.actions.ActionAfterGraphTransform
import life.plenty.model.connection.{Child, Connection}
import life.plenty.ui.model.DisplayModel

class DisplayUpdateOnChildrenTransform(override val withinOctopus: Octopus) extends ActionAfterGraphTransform {
  override def onConnectionAdd(connection: Connection[_]): Either[Exception, Unit] = {
    //    print("on connection add ")
    connection match {
      case _: Child[_] ⇒
        //        println("yes")
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
