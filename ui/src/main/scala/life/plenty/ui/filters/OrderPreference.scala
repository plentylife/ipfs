package life.plenty.ui.filters

import life.plenty.model.modifiers.OctopusOrdering
import life.plenty.model.{BasicSpace, Members, Octopus}

import scala.collection.TraversableLike
//
//trait OrderPreference[IT, L<: Iterable[IT], T <: Octopus] extends Module[T] {
//  def orderPreference(toReorder: L): L
//}
//
//trait ChildOrderPreference extends OrderPreference[Octopus, List[Octopus], BasicSpace]

class BasicSpaceDisplayOrder(override val withinOctopus: BasicSpace) extends OctopusOrdering[BasicSpace] {
  //  override def order(toReorder: List[Octopus]): List[Octopus] = {
  override def order[L <: TraversableLike[Octopus, L]](what: L): L = {
    val i = what.indexWhere(_.isInstanceOf[Members])
    if (i != -1) {
      val s = toReorder.splitAt(i)
      toReorder(i) :: s._1 ::: s._2.tail
    } else {
      toReorder
    }
  }
}
