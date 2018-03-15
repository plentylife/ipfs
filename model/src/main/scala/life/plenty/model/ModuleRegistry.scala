package life.plenty.model

import life.plenty.model.hub.definition.{Module, Hub}

object ModuleRegistry {
  private var _registry: List[PartialFunction[Hub, Module[Hub]]] = List()
  def getModules(octopus: Hub): List[Module[Hub]] = {
    //        println("giving modules", _registry)
    registry.flatMap(f ⇒ f(octopus))
  }
  def registry: List[Hub ⇒ Option[Module[Hub]]] = _registry.map(_.lift)
  def add(f: PartialFunction[Hub, Module[Hub]]): Unit = _registry = f :: _registry
}
