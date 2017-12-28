package life.plenty

import life.plenty.model._
import life.plenty.ui.model.DisplayModuleDefinitions.{ChildDisplay, ModularDisplay, TitleWithInput, TitleWithNav}

package object ui {

  def initialize(): Unit = {
    println("UI is adding modules into registry")

    /* the modules should be added in a queue fashion: the last overrides the first */

    ModuleRegistry add { case o: Space ⇒ new TitleWithNav(o) }
    ModuleRegistry add { case o: GreatQuestion ⇒ new TitleWithInput(o) }
    ModuleRegistry add { case o: Octopus ⇒ new ChildDisplay(o) }
    ModuleRegistry add { case o: Octopus ⇒ new ModularDisplay(o) }
  }

}
