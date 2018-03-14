package life.plenty.ui.filters

import life.plenty.model.modifiers.ModuleFilters
import life.plenty.model.octopi.definition.{Module, Hub}
import life.plenty.ui.display._
import life.plenty.ui.display.meta.{ChildDisplay, ModularDisplay}
import life.plenty.ui.model.DisplayModule
import life.plenty.ui.model.ViewState
import life.plenty.ui.model.ViewState.ViewState

//
abstract class RouterModuleFilter(override val hub: Hub) extends
  RouterFilter[Hub, Module[Hub], List[Module[Hub]]] with ModuleFilters[Hub] {

  override protected def filterInner(what: List[Module[Hub]]): List[Module[Hub]] = {
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

class RateEffortModuleFilter(private val _withinOctopus: Hub) extends RouterModuleFilter(_withinOctopus) {
  override protected val engageOnState: ViewState = ViewState.RATING
  private val _acceptable = {
    Set(
      !_.isInstanceOf[DisplayModule[_]],
      _.isInstanceOf[MembersCardDisplay],
      _.isInstanceOf[MenuBar],
      _.isInstanceOf[ViewStateLinks],
    _.isInstanceOf[RateEffortDisplay]
    ): Set[Module[_] ⇒ Boolean]
  }
  override protected def acceptable: Set[Module[_] ⇒ Boolean] = super.acceptable ++ _acceptable
}

class DiscussModuleFilter(private val _withinOctopus: Hub) extends RouterModuleFilter(_withinOctopus) {
  override protected val engageOnState: ViewState = ViewState.DISCUSSION
  private val _acceptable = {
    Set(
      !_.isInstanceOf[RateEffortDisplay]
    ): Set[Module[_] ⇒ Boolean]
  }
  override protected def acceptable: Set[Module[_] ⇒ Boolean] = super.acceptable ++ _acceptable
}