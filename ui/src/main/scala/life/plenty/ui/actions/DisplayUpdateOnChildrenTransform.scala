package life.plenty.ui.actions

import life.plenty.model.actions.ActionAfterGraphTransform
import life.plenty.model.connection._
import life.plenty.model.octopi._
import life.plenty.ui.display.meta.ChildDisplay
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

class DisplayUpdateWalletChange(override val withinOctopus: Octopus) extends ActionAfterGraphTransform {
  override def onConnectionAdd(connection: Connection[_]): Either[Exception, Unit] = onConnection(connection)

  override def onConnectionRemove(connection: Connection[_]): Either[Exception, Unit] = onConnection(connection)

  private def onConnection(c: Connection[_]): Either[Exception, Unit] = {
    c match {
      //      case Child(v: Vote) if v.by == UiContext.getUser ⇒ {
      //        CurrentUserWallet.update(withinOctopus)
      //      }
      //      case Child(t: Transaction) if t.from == UiContext.getUser ⇒ {
      //        CurrentUserWallet.update(withinOctopus)
      //      }
      //      case Child(va: VoteAllowance) if va.owner == UiContext.getUser ⇒ {
      //        CurrentUserWallet.update(withinOctopus)
      //      }
      case _ ⇒
    }
    Right()
  }
}


class DisplayUpdateAnswerOrderChange(override val withinOctopus: Answer) extends ActionAfterGraphTransform {
  override def onConnectionAdd(connection: Connection[_]): Either[Exception, Unit] = onConnection(connection)

  override def onConnectionRemove(connection: Connection[_]): Either[Exception, Unit] = onConnection(connection)

  private def onConnection(c: Connection[_]): Either[Exception, Unit] = {
    c match {
      case Child(v: Vote) ⇒ withinOctopus.getTopConnectionData({ case Parent(p: Octopus) ⇒ p }).foreach(p ⇒ {
        DisplayModel.reRender(p, { case dm: ChildDisplay ⇒ dm })
      })
      case _ ⇒
    }
    Right()
  }
}

