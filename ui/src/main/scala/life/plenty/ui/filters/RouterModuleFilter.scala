package life.plenty.ui.filters

import life.plenty.model.modifiers.ModuleFilters
import life.plenty.model.{Module, Octopus}
import life.plenty.ui.display._
import life.plenty.ui.model.DisplayModel.DisplayModule
import life.plenty.ui.model.ViewState.ViewState
import life.plenty.ui.model.{Router, ViewState}

abstract class RouterModuleFilter(override val withinOctopus: Octopus) extends
  ModuleFilters[Octopus] {

  protected val engageOnState: ViewState

  protected def acceptable: Set[(Module[_]) ⇒ Boolean] = Set(_.isInstanceOf[ChildDisplay],
    _.isInstanceOf[ModularDisplay])
  override def filter(what: List[Module[Octopus]]): List[Module[Octopus]] = if (isEngaged) {
    val f = what filter { m ⇒
      acceptable exists (cf ⇒ cf(m))
    }
    //    println("router filter filtered ", f)
    println("router filter filtered ", this)
    f
  } else what

  protected def isEngaged: Boolean = isEngaged(Router.router.state.value.stateId)

  protected def isEngaged(stateId: Int): Boolean = ViewState(stateId) == engageOnState
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