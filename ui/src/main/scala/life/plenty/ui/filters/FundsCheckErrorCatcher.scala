package life.plenty.ui.filters

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.actions.ActionCatchGraphTransformError
import life.plenty.model.octopi.definition.Hub
import life.plenty.model.permissions.{FundsError, NotEnoughThanks, NotEnoughVotingPower}
import life.plenty.ui
import life.plenty.ui.display.Modal
import life.plenty.ui.model.UiContext
import org.scalajs.dom.Node

class FundsCheckErrorCatcher(override val withinOctopus: Hub) extends ActionCatchGraphTransformError {
  override def catchError(e: Throwable): Unit = {
    e match {
      case e: NotEnoughThanks ⇒
        ui.console.error(e)
        Modal.setContentAndOpen(notEnoughThanks(e), "error", "close")
      case e: NotEnoughVotingPower ⇒
        ui.console.error(e)
        Modal.setContentAndOpen(notEnoughVotes(e), "error", "close")
      case _ ⇒ Unit
    }
  }

  private def address(e: FundsError) =
    if (UiContext.getUser.id == e.user.id) "You do" else s"${e.user.getNameOrEmpty.now} does"


  @dom
  private def notEnoughThanks(e: NotEnoughThanks): Binding[Node] = {
    <span>{address(e)} not have enough {ui.thanks}hanks<br/>
      You have to earn some by receiving {ui.thanks}hanks from the fellow members in the space for contributing
    </span>
  }

  @dom
  private def notEnoughVotes(e: NotEnoughVotingPower): Binding[Node] = {
    <span>{address(e)} do not have enough voting power<br/>
      You have to earn votes by spending {ui.thanks}thanks
    </span>
  }
}
