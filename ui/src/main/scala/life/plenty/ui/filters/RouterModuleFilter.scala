package life.plenty.ui.filters

import life.plenty.model.modifiers.ModuleFilters
import life.plenty.model.{Module, Octopus}
import life.plenty.ui.display._
import life.plenty.ui.model.DisplayModel.DisplayModule
import life.plenty.ui.model.ViewState
import life.plenty.ui.model.ViewState.ViewState

//(override val withinOctopus: Octopus)
trait RouterModuleFilter extends
  RouterFilter[Octopus, Module[Octopus], List[Module[Octopus]]] with ModuleFilters[Octopus] {

  override protected def filterInner(what: List[Module[Octopus]]): List[Module[Octopus]] = {
    val f = what filter { m ⇒
      acceptable exists (cf ⇒ cf(m))
    }
    //    println("router filter filtered ", this, f)
    //    println("router filter filtered ", this)
    f
  }
  protected def acceptable: Set[(Module[_]) ⇒ Boolean] = Set(_.isInstanceOf[ChildDisplay],
    _.isInstanceOf[ModularDisplay], _.isInstanceOf[RouterFilter[_, _, _]])
}

class RateEffortModuleFilter(override val withinOctopus: Octopus) extends RouterModuleFilter(withinOctopus) {
  override protected val engageOnState: ViewState = ViewState.RATING
  private val _acceptable = {
    Set(
      !_.isInstanceOf[DisplayModule[_]],
      _.isInstanceOf[MembersDisplay],
      _.isInstanceOf[TitleWithNav],
      _.isInstanceOf[ViewStateLinks],
    _.isInstanceOf[RateEffortDisplay]
    ): Set[Module[_] ⇒ Boolean]
  }
  override protected def acceptable: Set[Module[_] ⇒ Boolean] = super.acceptable ++ _acceptable
}

class DiscussModuleFilter(override val withinOctopus: Octopus) extends RouterModuleFilter(withinOctopus) {
  override protected val engageOnState: ViewState = ViewState.DISCUSSION
  private val _acceptable = {
    Set(
      !_.isInstanceOf[RateEffortDisplay]
    ): Set[Module[_] ⇒ Boolean]
  }
  override protected def acceptable: Set[Module[_] ⇒ Boolean] = super.acceptable ++ _acceptable
}