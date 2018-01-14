package life.plenty.ui.filters

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.modifiers.ModuleFilters
import life.plenty.model.{Module, Octopus}
import life.plenty.ui.display._
import life.plenty.ui.model.DisplayModel.DisplayModule
import life.plenty.ui.model.ViewState.ViewState
import life.plenty.ui.model.{DisplayModel, Router, ViewState}
import org.scalajs.dom.Node

abstract class RouterModuleFilter(override val withinOctopus: Octopus) extends
  ModuleFilters[Octopus] with DisplayModule[Octopus] {

  protected val engageOnState: ViewState

  protected def acceptable: Set[(Module[_]) ⇒ Boolean] = Set(_.isInstanceOf[ChildDisplay],
    _.isInstanceOf[ModularDisplay])
  override def filter(what: List[Module[Octopus]]): List[Module[Octopus]] = if (isEngaged) {
    val f = what filter { m ⇒
      acceptable exists (cf ⇒ cf(m))
    }
    //    println("router filter filtered ", f)
    f
  } else what
  protected def isEngaged: Boolean = isEngaged(Router.router.state.value.stateId)

  override def update(): Unit = Unit
  @dom
  override protected def generateHtml(overrides: List[DisplayModel.ModuleOverride]): Binding[Node] = {
    println("has router filter rendered", hasRendered)
    <span class="no-display">
      {println("has router filter rendered", hasRendered, Router.paramsOnLoad.map(_.stateId == Router.router.state
      .bind.stateId))
    if (hasRendered) {
      println("has rendered is true")
      if (Router.paramsOnLoad.map(_.stateId != Router.router.state.bind.stateId).getOrElse(false) &&
        isEngaged(Router.router.state.bind.stateId)) {
        println("router filter updated", withinOctopus);
        DisplayModel.reRender(withinOctopus);
      }
    } else println("has rendered is false")
    ""}
    </span>
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

class DiscussModuleFilter(override val withinOctopus: Octopus) extends RouterModuleFilter(withinOctopus) {
  override protected val engageOnState: ViewState = ViewState.DISCUSSION
  private val _acceptable = {
    Set(
      !_.isInstanceOf[RateEffortDisplay]
    ): Set[Module[_] ⇒ Boolean]
  }
  override protected def acceptable: Set[Module[_] ⇒ Boolean] = super.acceptable ++ _acceptable
}