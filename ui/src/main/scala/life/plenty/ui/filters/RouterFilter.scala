package life.plenty.ui.filters

import life.plenty.model.filters.ModuleFilters
import life.plenty.model.{Module, Octopus}
import life.plenty.ui.display.RateEffortDisplay

import scala.collection.TraversableLike

abstract class RouterModuleFilter(override val withinOctopus: Octopus) extends ModuleFilters[Octopus] {
  protected val acceptable: Set[(Module[_]) ⇒ Boolean]

  override def filter[L <: TraversableLike[Module[_], L]](what: L): L = {
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
