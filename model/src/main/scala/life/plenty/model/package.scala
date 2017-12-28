package life.plenty

package object model {
  def initialize(): Unit = {
    println("Model is adding modules to registry")
    ModuleRegistry.add { case o: Space ⇒ new AddGreatQuestions(o) }
  }
}
