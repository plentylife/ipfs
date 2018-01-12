package life.plenty.ui.filters

import life.plenty.model.modifiers.OctopusOrdering
import life.plenty.model.{BasicSpace, Members, Octopus}
//
//trait OrderPreference[IT, L<: Iterable[IT], T <: Octopus] extends Module[T] {
//  def orderPreference(toReorder: L): L
//}
//
//trait ChildOrderPreference extends OrderPreference[Octopus, List[Octopus], BasicSpace]

class BasicSpaceDisplayOrder(override val withinOctopus: BasicSpace) extends OctopusOrdering[BasicSpace] {

  override def order(what: List[Octopus]): List[Octopus] = {
    val i = what.indexWhere(_.isInstanceOf[Members])
    if (i != -1) {
      val s = what.splitAt(i)
      what(i) :: s._1 ::: s._2.tail
    } else {
      what
    }
  }
}
