package life.plenty

import life.plenty.model._
import life.plenty.ui.actions.DisplayUpdateOnChildrenTransform
import life.plenty.ui.display._
import life.plenty.ui.display.actions.{Contribute, CreateAnswer}
import life.plenty.ui.filters.{BasicSpaceDisplayOrder, RateEffortModuleFilter}

package object ui {

  def initialize(): Unit = {
    println("UI is adding modules into registry")

    /* the modules should be added in a list fashion: the last overrides the first */
    ModuleRegistry add { case o: Members ⇒ new MembersDisplay(o) }

    ModuleRegistry add { case o: Space ⇒ new TitleWithNav(o) }
    ModuleRegistry add { case o: Space ⇒ new ViewStateLinks(o) }
    ModuleRegistry add { case o: Space ⇒ new RateEffortModuleFilter(o) }
    ModuleRegistry add { case o: BasicSpace ⇒ new RateEffortModuleFilter(o) }
    ModuleRegistry add { case o: BasicSpace ⇒ new RateEffortDisplay(o) }
    ModuleRegistry add { case o: BasicSpace ⇒ new BasicSpaceDisplayOrder(o) }

    ModuleRegistry add { case o: GreatQuestion ⇒ new TitleWithQuestionInput(o) }

    ModuleRegistry add { case q: Question ⇒ new QuestionTitle(q) }
    ModuleRegistry add { case q: Question ⇒ new CreateAnswer(q) }

    ModuleRegistry add { case a: Answer ⇒ new AnswerDisplay(a) }
    ModuleRegistry add { case c: Contribution ⇒ new ContributionDisplay(c) }
    ModuleRegistry add { case c: Contribution ⇒ new Contribute(c) }


    ModuleRegistry add { case o: Octopus ⇒ new DisplayUpdateOnChildrenTransform(o) }
    ModuleRegistry add { case o: Octopus ⇒ new ChildDisplay(o) }
    ModuleRegistry add { case o: Octopus ⇒ new ModularDisplay(o) }
  }

}
