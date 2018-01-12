package life.plenty.model.modifiers

import life.plenty.model.{Module, Octopus}

import scala.collection.TraversableLike

trait CollectionModificationModule[O <: Octopus, Elem, L <: TraversableLike[Elem, L]] extends Module[O] {
  def process(what: L): L
}

trait FilterModule[O <: Octopus, F] extends CollectionModificationModule[O, F, L

<: TraversableLike[F, L]] {
def process[L <: TraversableLike[F, L]] (what: L): L = filter (what)
  def filter[L <: TraversableLike[F, L]](what: L): L
}

trait ModuleFilters[O <: Octopus] extends FilterModule[O, Module[_]]

trait OctopusOrdering[O <: Octopus] extends CollectionModificationModule[O, Octopus] {
  def process[L <: TraversableLike[Octopus, L]](what: L): L = order(what)

  def order[L <: TraversableLike[Octopus, L]](what: L): L
}