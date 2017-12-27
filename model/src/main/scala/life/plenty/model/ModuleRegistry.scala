package life.plenty.model

object ModuleRegistry {
  var _registry: List[PartialFunction[Octopus, Module[_]]] = List()
  def getModules(octopus: Octopus): List[Module[_]] = {
    registry.flatMap(f ⇒ f(octopus))
  }
  def registry = _registry.map(_.lift)
}
