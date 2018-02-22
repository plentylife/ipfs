package life.plenty.ui.filters

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.actions.ActionCatchGraphTransformError
import life.plenty.model.octopi.definition.Hub
import life.plenty.model.security.{FundsError, NotEnoughThanks, NotEnoughVotingPower}
import life.plenty.ui
import life.plenty.ui.display.{ErrorModal, Modal}
import life.plenty.ui.model.UiContext
import org.scalajs.dom.Node

class FundsCheckErrorCatcher(override val withinOctopus: Hub) extends ActionCatchGraphTransformError {
  override def catchError(e: Throwable): Unit = {
    e match {
      case e: NotEnoughThanks ⇒
        ui.console.error(e)
        if (UiContext.getUser.id == e.user.id) ErrorModal.setContentAndOpen(notEnoughThanks(e))
      case e: NotEnoughVotingPower ⇒
        ui.console.error(e)
        if (UiContext.getUser.id == e.user.id) ErrorModal.setContentAndOpen(notEnoughVotes(e))
      case _ ⇒ Unit
    }
  }

  @deprecated
  private def address(e: FundsError) =
    if (UiContext.getUser.id == e.user.id) "You do" else s"${e.user.getNameOrEmpty.now} does"


  @dom
  private def notEnoughThanks(e: NotEnoughThanks): Binding[Node] = {
    <span>{address(e)} not have enough {ui.thanks}hanks<br/>
      You have to earn some by receiving {ui.thanks}hanks from your fellow members in the space for contributing
    </span>
  }

  @dom
  private def notEnoughVotes(e: NotEnoughVotingPower): Binding[Node] = {
    <span>{address(e)} do not have enough voting power<br/>
      You have to earn votes by spending {ui.thanks}thanks. Give some to your fellow members for contributing!
    </span>
  }
}
