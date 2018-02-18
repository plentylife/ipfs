package life.plenty

import life.plenty.model._
import life.plenty.model.octopi._
import life.plenty.model.octopi.definition.Hub
import life.plenty.model.utils.Console
import life.plenty.ui.display._
import life.plenty.ui.display.actions._
import life.plenty.ui.display.info.ThanksGiven
import life.plenty.ui.display.meta.{ChildDisplay, ModularDisplay}
import life.plenty.ui.filters.BasicSpaceDisplayOrder

package object ui {

  val console = new Console(true, true)
  val thanks = "\u20B8"

  def initialize(): Unit = {
    println("UI is adding modules into registry")

    /* the modules should be added in a list fashion: the last overrides the first */

    ModuleRegistry add { case a: Contribution ⇒ new ThanksGiven(a) }

    ModuleRegistry add { case o: Space ⇒ new MenuBar(o) } // here so that it is displayed on top
    ModuleRegistry add { case o: Space ⇒ new SpaceActionsBar(o) }
    ModuleRegistry add { case o: Space ⇒ new CreateAnswerButton(o) }
    ModuleRegistry add { case o: Space ⇒ new CreateQuestionButton(o) }
    ModuleRegistry add { case o: Space ⇒ new CreateSpaceButton(o) }
    ModuleRegistry add { case o: SignupQuestion ⇒ new SignupButton(o) }
    ModuleRegistry add { case a: Contribution ⇒ new GiveButton(a) }
    ModuleRegistry add { case a: Answer ⇒ new VoteButtons(a) }

    ModuleRegistry add { case q: Question ⇒ new InlineQuestionDisplay(q) }

    ModuleRegistry add { case a: Answer ⇒ new ConfirmActionDisplay(a) }
    ModuleRegistry add { case a: Proposal ⇒ new ProposalDisplay(a) }
    ModuleRegistry add { case c: Contribution ⇒ new ContributionDisplay(c) }

    ModuleRegistry add { case o: Hub if !(o.isInstanceOf[Vote] || o.isInstanceOf[Question] ||
      o.isInstanceOf[Space]) ⇒      new ChildDisplay(o) }

    ModuleRegistry add { case o: Hub ⇒ new ModularDisplay(o) }

    ModuleRegistry add { case o: User ⇒ new FullUserBadge(o) }
    ModuleRegistry add { case o: Members ⇒ new MembersCardDisplay(o) }

    ModuleRegistry add { case o: Space ⇒ new CardSpaceDisplay(o) } // can apply to question too
    ModuleRegistry add { case o: Question ⇒ new CardQuestionDisplay(o) }
    ModuleRegistry add { case o: Answer ⇒ new CardAnswerDisplay(o) }
    ModuleRegistry add { case o: Space ⇒ new TopSpaceLayout(o) }
    ModuleRegistry add { case o: SignupQuestion ⇒ new SignupQuestionSpaceLayout(o) }

  }

}
//    ModuleRegistry add { case o: Question ⇒ new QuestionModuleGroup(o) }

// following the model of one contributer per contribution/answer
//    ModuleRegistry add { case c: Contribution ⇒ new Contribute(c) }

//    ModuleRegistry add { case o: Octopus ⇒ new RateEffortModuleFilter(o) }
//    ModuleRegistry add { case o: Octopus ⇒ new DiscussModuleFilter(o) }
//    ModuleRegistry add { case o: Octopus ⇒ new RateEffortConnectionFilter(o) }


//    ModuleRegistry add { case o: Event ⇒ new EventCardDisplay(o) }
//    ModuleRegistry add { case o: Space ⇒ new EditSpace(o) }
//
//    ModuleRegistry add { case o: ContainerSpace ⇒ new DescriptionDisplay(o) }
//    ModuleRegistry add { case o: Event ⇒ new DescriptionDisplay(o) }

//    ModuleRegistry add { case o: Space ⇒ new ViewStateLinks(o) }
//    ModuleRegistry add { case o: BasicSpace ⇒ new RateEffortDisplay(o) }
//    ModuleRegistry add { case o: ContainerSpace ⇒ new BasicSpaceDisplayOrder(o) }
//    ModuleRegistry add { case o: Space ⇒ new TopSpaceGroups(o) }

//    ModuleRegistry add { case o: GreatQuestion ⇒ new TitleWithQuestionInput(o) }

//    ModuleRegistry add { case q: Question ⇒ new QuestionTitle(q) }
//    ModuleRegistry add { case q: Question ⇒ new CreateAnswer(q) }
//    ModuleRegistry add { case q: Question ⇒ new AnswerGroup(q) }
