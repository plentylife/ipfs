package life.plenty.ui.filters

import life.plenty.model.modifiers.ModuleFilters
import life.plenty.model.{Module, Octopus}
import life.plenty.ui.display.RateEffortDisplay

abstract class RouterModuleFilter(override val withinOctopus: Octopus) extends
  ModuleFilters[Octopus] {
  protected val acceptable: Set[(Module[_]) ⇒ Boolean]

  override def filter(what: Iterable[Module[_]]): Iterable[Module[_]] = {
    what filter { m ⇒
      acceptable exists (cf ⇒ cf(m))
    }
  }
}

class RateEffortModuleFilter(override val withinOctopus: Octopus) extends RouterModuleFilter(withinOctopus) {
  override protected val acceptable: Set[Module[_] ⇒ Boolean] = Set(
    _.isInstanceOf[RateEffortDisplay]
  )
}
