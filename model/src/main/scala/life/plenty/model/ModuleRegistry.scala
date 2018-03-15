package life.plenty.model

import life.plenty.model.hub.definition.{Hub, Module}
import life.plenty.model.hub.pseudo.StrongHub

object ModuleRegistry {
  private var _registry: List[PartialFunction[Hub, Module[Hub]]] = List()

  def getModules(hub: Hub): List[Module[Hub]] = hub match {
    case _: StrongHub ⇒ List()
    case _ ⇒ registry.flatMap(f ⇒ f(hub))
  }

  def registry: List[Hub ⇒ Option[Module[Hub]]] = _registry.map(_.lift)
  def add(f: PartialFunction[Hub, Module[Hub]]): Unit = _registry = f :: _registry
}
