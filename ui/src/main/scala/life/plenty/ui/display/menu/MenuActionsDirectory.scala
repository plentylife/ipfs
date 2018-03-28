package life.plenty.ui.display.menu

import life.plenty.model.hub.definition.Hub
import life.plenty.ui.model.{SimpleDisplayModule, SimpleDisplayModuleDirectory}

object MenuActionsDirectory extends SimpleDisplayModuleDirectory[Hub] {
  override val directory: List[SimpleDisplayModule[_]] = List(
    DeleteButton
  )
}
