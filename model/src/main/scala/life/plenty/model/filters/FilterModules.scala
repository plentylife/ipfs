package life.plenty.model.filters

import life.plenty.model.{Module, Octopus}

import scala.collection.TraversableLike

trait FilterModule[O <: Octopus, F] extends Module[O] {
  def filter[L <: TraversableLike[F, L]](what: L): L
}

trait ModuleFilters[O <: Octopus] extends FilterModule[O, Module[_]]