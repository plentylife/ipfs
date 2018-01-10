package life.plenty.model

object ModuleRegistry {
  private var _registry: List[PartialFunction[Octopus, Module[Octopus]]] = List()
  def getModules(octopus: Octopus): List[Module[Octopus]] = {
    println("giving modules", _registry)
    registry.flatMap(f ⇒ f(octopus))
  }
  def registry: List[Octopus ⇒ Option[Module[Octopus]]] = _registry.map(_.lift)
  def add(f: PartialFunction[Octopus, Module[Octopus]]): Unit = _registry = f :: _registry
}
