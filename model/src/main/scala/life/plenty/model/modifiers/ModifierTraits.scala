package life.plenty.model.modifiers

import life.plenty.model.{Module, Octopus}

trait CollectionModificationModule[O <: Octopus, Elem] extends Module[O] {
  def apply[L <: Iterable[Elem]](what: L): L
}

trait FilterModule[O <: Octopus, F, L <: Iterable[F]] extends CollectionModificationModule[O, F, L] {
  def apply(what: L): L = filter(what)

  def filter(what: L): L
}

trait ModuleFilters[O <: Octopus, L <: Iterable[Module[_]]] extends FilterModule[O, Module[_], L]

trait OctopusModifier[Within <: Octopus, L <: Iterable[Octopus]] extends
  CollectionModificationModule[Within, Octopus, L]

trait OctopusOrdering[O <: Octopus] extends OctopusModifier[O, List[Octopus]] {
  def apply(what: List[Octopus]): List[Octopus] = order(what)

  def order(what: List[Octopus]): List[Octopus]
}