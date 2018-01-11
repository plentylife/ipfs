package life.plenty

import life.plenty.model.actions._

package object model {
  def initialize(): Unit = {
    println("Model is adding modules to registry")

    ModuleRegistry.add { case q: Question ⇒ new ActionCreateQuestion(q) }
    ModuleRegistry.add { case q: Question ⇒ new ActionCreateAnswer(q) }
    ModuleRegistry.add { case q: GreatQuestion ⇒ new ActionCreateQuestion(q) }

    ModuleRegistry.add { case a: Answer ⇒ new ActionCreateQuestion(a) }
    ModuleRegistry.add { case c: Contribution ⇒ new ActionAddContributor(c) }

    ModuleRegistry.add { case wp: WithParent[_] ⇒ new ActionAddParent(wp) }
    ModuleRegistry.add { case o: BasicSpace ⇒ new AddGreatQuestions(o) }
    ModuleRegistry.add { case o: BasicSpace ⇒ new InitializeMembers(o) }

    ModuleRegistry.add { case o: Octopus ⇒ new ActionAddMember(o) }
  }
}
