package life.plenty.ui.filters

import life.plenty.model._
import life.plenty.model.connection.Connection
import life.plenty.model.modifiers.ConnectionFilters
import life.plenty.ui.model.ViewState
import life.plenty.ui.model.ViewState.ViewState

abstract class RouterConnectionFilter[+O <: Octopus] extends RouterFilter[O, Connection[_], List[Connection[_]]]
  with ConnectionFilters[O] {
  protected def acceptable: Set[Any ⇒ Boolean]

  override protected def filterInner(what: List[Connection[_]]): List[Connection[_]] = what filter { c ⇒
    acceptable exists (cf ⇒ cf(c.value))
  }
}

class RateEffortConnectionFilter(override val withinOctopus: Octopus) extends RouterConnectionFilter[Octopus] {
  override protected val engageOnState: ViewState = ViewState.RATING
  private val _a: Set[Any ⇒ Boolean] = Set(_.isInstanceOf[Members])
  override protected def acceptable: Set[Any ⇒ Boolean] = _a
  override protected def filterInner(what: List[Connection[_]]): List[Connection[_]] = {
    //    println("con filter", withinOctopus)
    val res = if (withinOctopus.isInstanceOf[WithMembers]) super.filterInner(what) else List()
    println(res)
    res
  }
}
