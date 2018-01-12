package life.plenty.ui.filters

import com.thoughtworks.binding.dom
import life.plenty.model.modifiers.ModuleFilters
import life.plenty.model.{Module, Octopus}
import life.plenty.ui.display._
import life.plenty.ui.model.DisplayModel.DisplayModule
import life.plenty.ui.model.ViewState.ViewState
import life.plenty.ui.model.{DisplayModel, Router, ViewState}

abstract class RouterModuleFilter(override val withinOctopus: Octopus) extends
  ModuleFilters[Octopus] {

  protected val engageOnState: ViewState
  override def filter(what: List[Module[Octopus]]): List[Module[Octopus]] = if (isEngaged) {
    what filter { m ⇒
      acceptable exists (cf ⇒ cf(m))
    }
  } else what
  protected def acceptable: Set[(Module[_]) ⇒ Boolean] = Set(_.isInstanceOf[ChildDisplay],
    _.isInstanceOf[ModularDisplay])
  protected def isEngaged: Boolean = ViewState(Router.router.state.value.stateId) == engageOnState
  @dom
  private def update = {
    isEngaged(Router.router.state.bind.stateId) && {
      println("router filter updated");
      DisplayModel.reRender(withinOctopus);
      false
    }
  }
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
