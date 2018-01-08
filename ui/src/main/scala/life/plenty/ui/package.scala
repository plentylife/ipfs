package life.plenty

import life.plenty.model._
import life.plenty.ui.actions.DisplayUpdateOnChildrenTransform
import life.plenty.ui.display._

package object ui {

  def initialize(): Unit = {
    println("UI is adding modules into registry")

    /* the modules should be added in a queue fashion: the last overrides the first */

    ModuleRegistry add { case o: Space ⇒ new TitleWithNav(o) }
    ModuleRegistry add { case o: Space ⇒ new DisplayUpdateOnChildrenTransform(o) }
    ModuleRegistry add { case o: GreatQuestion ⇒ new TitleWithQuestionInput(o) }
    ModuleRegistry add { case q: Question ⇒ new QuestionTitle(q) }
    ModuleRegistry add { case o: Octopus ⇒ new ChildDisplay(o) }
    ModuleRegistry add { case o: Octopus ⇒ new ModularDisplay(o) }
  }

}
