package life.plenty

import life.plenty.model.{ModuleRegistry, Octopus, Space}
import life.plenty.ui.model.DisplayModuleDefinitions.{ModularDisplay, TitleWithNav}

package object ui {

  def initialize(): Unit = {
    println("adding modules into registry")

    /* the modules should be added in a queue fashion: the last overrides the first */

    ModuleRegistry add { case o: Space ⇒ new TitleWithNav(o) }
    ModuleRegistry add { case o: Octopus ⇒ new ModularDisplay(o) }
  }

}
