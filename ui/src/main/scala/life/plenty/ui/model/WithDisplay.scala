package life.plenty.ui.model

import com.thoughtworks.binding.Binding
import life.plenty.model._

trait Displayable extends Octopus {
  override val modules: Set[Module] = super.modules + displayModule
  val displayModule: DisplayModule[Binding[_]]
}

class SpaceWithUI(override val title) extends Space with Displayable {
}
