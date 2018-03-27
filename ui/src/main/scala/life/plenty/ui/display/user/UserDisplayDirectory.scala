package life.plenty.ui.display.user

import life.plenty.model.hub.User
import life.plenty.ui.model.{SimpleDisplayModule, SimpleDisplayModuleDirectory}

object UserDisplayDirectory extends SimpleDisplayModuleDirectory[User] {
override val directory: List[SimpleDisplayModule[User]] = List(
  JustTheName, FullUserBadge
)
}
