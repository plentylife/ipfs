package life.plenty.model.modifiers

import life.plenty.model.connection.DataHub
import life.plenty.model.octopi.definition.{Module, Hub}
import rx.{Ctx, Rx}

trait CollectionModificationModule[+O <: Hub, Elem, L <: Iterable[Elem]] extends Module[O] {
  def apply(what: L): L
}

trait FilterModule[+O <: Hub, Elem, L <: Iterable[Elem]] extends CollectionModificationModule[O, Elem, L] {
  def apply(what: L): L = filter(what)

  def filter(what: L): L
}

/** can in theory filter themselves out */
trait ModuleFilters[+O <: Hub] extends FilterModule[O, Module[Hub], List[Module[Hub]]]

trait ConnectionFilters[+O <: Hub] extends FilterModule[O, DataHub[_], List[DataHub[_]]]

//trait RxConnectionFilters[+O <: Octopus] extends FilterModule[O, Connection[_], Rx[List[Connection[_]]]]
trait RxConnectionFilters[+O <: Hub] extends Module[O] {
  def apply(what: Rx[Option[DataHub[_]]])(implicit ctx: Ctx.Owner): Rx[Option[DataHub[_]]]
}

trait OctopusModifier[+Within <: Hub] extends
  CollectionModificationModule[Within, Hub, List[Hub]] {

  def applyRx(whatRx: Rx[List[Hub]])(implicit ctx: Ctx.Owner): Rx[List[Hub]]
}

trait OctopusOrdering[+O <: Hub] extends OctopusModifier[O] {
  def apply(what: List[Hub]): List[Hub] = order(what)

  def order(what: List[Hub]): List[Hub]
}