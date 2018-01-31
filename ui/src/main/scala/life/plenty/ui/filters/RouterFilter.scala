package life.plenty.ui.filters

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.modifiers.FilterModule
import life.plenty.model.octopi.Octopus
import life.plenty.ui.model.DisplayModel.DisplayModule
import life.plenty.ui.model.ViewState.ViewState
import life.plenty.ui.model.{Router, ViewState}
import org.scalajs.dom.raw.Node

trait RouterFilter[+O <: Octopus, Elem, L <: Iterable[Elem]] extends FilterModule[O, Elem, L] with
  DisplayModule[Octopus] {
  protected val engageOnState: ViewState

  def filter(what: L): L = if (isEngaged) {filterInner(what)} else what

  protected def isEngaged: Boolean = isEngaged(Router.router.state.value.stateId)

  protected def isEngaged(stateId: Int): Boolean = ViewState(stateId) == engageOnState

  override def update(): Unit = Unit

  protected def filterInner(what: L): L

  @dom
  override protected def generateHtml(): Binding[Node] = {
    <span class="module-hook">
      {Router.reRender(withinOctopus, Router.router.state.bind); ""}
    </span>
  }
}
