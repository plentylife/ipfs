package life.plenty

package object model {
  def initialize(): Unit = {
    println("Model is adding modules to registry")
    ModuleRegistry.add { case wp: WithParent[_] ⇒ new ActionAddParent(wp) }
    ModuleRegistry.add { case o: Space ⇒ new AddGreatQuestions(o) }
  }
}
