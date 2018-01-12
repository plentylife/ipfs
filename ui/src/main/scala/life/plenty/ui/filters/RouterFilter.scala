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
  ModuleFilters[Octopus] {

  protected val engageOnState: ViewState
  private var _flag = false
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
  @dom
  def update: Binding[Node] = {
    //    println("rf updating", Router.router.state.bind.stateId, ViewState(Router.router.state.bind.stateId),
    // engageOnState)

    <span class="no-display">
      {println(isEngaged(Router.router.state.bind.stateId), _flag)
    //        if (_flag) {
    if (isEngaged(Router.router.state.bind.stateId)) {
      println("router filter updated", withinOctopus);
      DisplayModel.reRender(withinOctopus);
    }
    //        } else {
    //          _flag = true
    //        }
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
