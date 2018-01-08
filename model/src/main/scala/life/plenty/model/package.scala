package life.plenty

import life.plenty.model.actions.ActionCreateQuestion

package object model {
  def initialize(): Unit = {
    println("Model is adding modules to registry")

    ModuleRegistry.add { case q: Question ⇒ new ActionCreateQuestion(q) }
    ModuleRegistry.add { case wp: WithParent[_] ⇒ new ActionAddParent(wp) }
    ModuleRegistry.add { case o: BasicSpace ⇒ new AddGreatQuestions(o) }
  }
}
