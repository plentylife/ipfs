package life.plenty

import life.plenty.model.{ModuleRegistry, Space}
import life.plenty.ui.model.DisplayModuleDefinitions.TitleWithNav

package object ui {

  def initialize(): Unit = {
    println("adding modules into registry")

    ModuleRegistry add { case o: Space ⇒ new TitleWithNav(o) }
  }

}
