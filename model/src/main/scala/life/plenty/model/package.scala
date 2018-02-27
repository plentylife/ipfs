package life.plenty


import life.plenty.model.actions._
import life.plenty.model.modifiers.{AnswerVoteOrder, InactiveFilter}
import life.plenty.model.octopi._
import life.plenty.model.octopi.definition.Hub
import life.plenty.model.security.FundsCheck
import life.plenty.model.utils.{Console, Hash}

package object model {
  val console = new Console(false, false, "Model")
  private var _hasher: Hash = _
  private var _defaultCreator: Option[User] = None

  def getHasher: Hash = _hasher

  def setHasher(h: Hash) = _hasher = h

  def defaultCreator_=(u: User) = _defaultCreator = Option(u)

  def defaultCreator = _defaultCreator

  def initialize(): Unit = {
    println("Model is adding modules to registry")

    ModuleRegistry.add { case u: User ⇒ new FundsCheck(u) }

    ModuleRegistry.add { case q: Question ⇒ new ActionAddConfirmedMarker(q) }
    ModuleRegistry.add { case q: Question ⇒ new ActionCreateQuestion(q) }
    ModuleRegistry.add { case q: Question ⇒ new ActionCreateAnswer(q) }
    ModuleRegistry.add { case q: SignupQuestion ⇒ new ActionSignup(q) }
    ModuleRegistry.add { case q: Question ⇒ new AnswerVoteOrder(q) }
    ModuleRegistry.add { case q: Space ⇒ new ActionCreateQuestion(q) }

    ModuleRegistry.add { case a: Answer ⇒ new ActionAddConfirmedMarker(a) }
    ModuleRegistry.add { case a: Answer ⇒ new ActionCreateQuestion(a) }
    ModuleRegistry.add { case a: Answer ⇒ new ActionUpDownVote(a) }

    ModuleRegistry.add { case o: Contribution ⇒ new ActionGiveThanks(o) }

    // one contributor per contribution (the creator)
    //    ModuleRegistry.add { case c: Contribution ⇒ new ActionAddContributor(c) }

    ModuleRegistry.add { case o: ContainerSpace ⇒ new ActionAddDescription(o) }
    ModuleRegistry.add { case o: Event ⇒ new ActionAddDescription(o) }

    ModuleRegistry.add { case o: ContainerSpace ⇒ new InitializeMembersOctopus(o) }
    ModuleRegistry.add { case o: WithMembers ⇒ new ActionAddMember(o) }

    ModuleRegistry.add { case o: Space ⇒ new ActionMove(o) }

    ModuleRegistry.add { case o: Hub ⇒ new ActionRemove(o) }
    ModuleRegistry.add { case o: Hub ⇒ new InactiveFilter(o) }

  }
}
