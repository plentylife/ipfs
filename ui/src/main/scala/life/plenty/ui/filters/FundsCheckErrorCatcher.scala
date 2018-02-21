package life.plenty.ui.filters

import life.plenty.model.actions.ActionCatchGraphTransformError
import life.plenty.model.octopi.definition.Hub
import life.plenty.ui

class FundsCheckErrorCatcher(override val withinOctopus: Hub) extends ActionCatchGraphTransformError {
  override def catchError(e: Exception): Unit = {
    ui.console.error(e)
  }
}
