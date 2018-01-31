package life.plenty


import life.plenty.model.actions._
import life.plenty.model.modifiers.AnswerVoteOrder
import life.plenty.model.octopi._
import life.plenty.model.utils.{Console, Hash}

package object model {
  val console = new Console(true)
  private var _hasher: Hash = _
  private var _defaultCreator: Option[User] = None

  def getHasher: Hash = _hasher

  def setHasher(h: Hash) = _hasher = h

  def defaultCreator_=(u: User) = _defaultCreator = Option(u)

  def defaultCreator = _defaultCreator

  def initialize(): Unit = {
    println("Model is adding modules to registry")

    ModuleRegistry.add { case q: Question ⇒ new ActionCreateQuestion(q) }
    ModuleRegistry.add { case q: Question ⇒ new ActionCreateAnswer(q) }
    ModuleRegistry.add { case q: Question ⇒ new AnswerVoteOrder(q) }
    ModuleRegistry.add { case q: GreatQuestion ⇒ new ActionCreateQuestion(q) }

    ModuleRegistry.add { case a: Answer ⇒ new ActionCreateQuestion(a) }
    ModuleRegistry.add { case a: Answer ⇒ new ActionUpDownVote(a) }

    ModuleRegistry.add { case o: Contribution ⇒ new ActionTip(o) }

    // one contributor per contribution (the creator)
    //    ModuleRegistry.add { case c: Contribution ⇒ new ActionAddContributor(c) }

    //    ModuleRegistry.add { case wp: WithParent[_] ⇒ new ActionAddParent(wp) }
    ModuleRegistry.add { case o: Event ⇒ new AddGreatQuestions(o) }
    ModuleRegistry.add { case o: BasicSpace ⇒ new AddGreatQuestions(o) }

    ModuleRegistry.add { case o: BasicSpace ⇒ new InitializeMembersOctopus(o) }
    ModuleRegistry.add { case o: WithMembers ⇒ new ActionAddMember(o) }
  }
}
