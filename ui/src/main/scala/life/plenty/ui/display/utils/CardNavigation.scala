package life.plenty.ui.display.utils

import life.plenty.model.hub.definition.Hub
import life.plenty.ui.model.DisplayModule
import life.plenty.ui.model.Router
import org.scalajs.dom.raw.Event

trait CardNavigation {self: DisplayModule[Hub] ⇒
  def navigateTo(e: Event) = Router.navigateToHub(self.hub)
}
