package life.plenty.ui.actions

import life.plenty.model.actions.ActionAfterGraphTransform
import life.plenty.model.connection.{Child, Connection, Contributor, Member}
import life.plenty.model.octopi.Octopus
import life.plenty.ui.display.{ContributionDisplay, MembersDisplay}
import life.plenty.ui.model.DisplayModel

class DisplayUpdateOnChildrenTransform(override val withinOctopus: Octopus) extends ActionAfterGraphTransform {
  override def onConnectionAdd(connection: Connection[_]): Either[Exception, Unit] = {
    //    println("re-render listener triggered", withinOctopus, connection)
    connection match {
      case Child(_) ⇒
        //        println("re-render listener executed", withinOctopus, connection)
        // todo fixme this funcion is flawed
        DisplayModel.reRender(withinOctopus)
      case Contributor(_) ⇒
        //        println("re-render listener executed for contributor", withinOctopus, connection)
        DisplayModel.reRender(withinOctopus, { case m: ContributionDisplay ⇒ m })
      case Member(_) ⇒
        //        println("re-render listener executed for member", withinOctopus, connection)
        DisplayModel.reRender(withinOctopus, { case m: MembersDisplay ⇒ m })
      case _ ⇒
    }
    Right()
  }

  override def onConnectionRemove(connection: Connection[_]): Either[Exception, Unit] = ???
}
