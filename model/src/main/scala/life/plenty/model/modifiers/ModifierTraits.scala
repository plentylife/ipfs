package life.plenty.model.modifiers

import life.plenty.model.connection.Connection
import life.plenty.model.{Module, Octopus}

trait CollectionModificationModule[+O <: Octopus, Elem, L <: Iterable[Elem]] extends Module[O] {
  def apply(what: L): L
}

trait FilterModule[+O <: Octopus, Elem, L <: Iterable[Elem]] extends CollectionModificationModule[O, Elem, L] {
  def apply(what: L): L = filter(what)

  def filter(what: L): L
}

/** can in theory filter themselves out */
trait ModuleFilters[+O <: Octopus] extends FilterModule[O, Module[Octopus], List[Module[Octopus]]]

trait ConnectionFilters[+O <: Octopus] extends FilterModule[O, Connection[_], List[Connection[_]]]

trait OctopusModifier[+Within <: Octopus] extends
  CollectionModificationModule[Within, Octopus, List[Octopus]] {
}

trait OctopusOrdering[+O <: Octopus] extends OctopusModifier[O] {
  def apply(what: List[Octopus]): List[Octopus] = order(what)

  def order(what: List[Octopus]): List[Octopus]
}