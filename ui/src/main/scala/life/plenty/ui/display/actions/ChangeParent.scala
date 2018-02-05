package life.plenty.ui.display.actions

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.actions.ActionMove
import life.plenty.model.octopi.Octopus
import life.plenty.ui.model.DisplayModel
import org.scalajs.dom.Event
import org.scalajs.dom.raw.Node

import scalaz.std.option._

object ChangeParent extends ControlDisplayWithState[ActionMove] {

  protected override def findDependant(o: Octopus) = if (!activeModule.exists(_.withinOctopus == o)) {
    o.getTopModule({ case a: ActionMove ⇒ a })
  } else None

  private var activeModule: Option[ActionMove] = None

  @dom
  def inactiveDisplay(o: Octopus)(implicit d: ActionMove): Binding[Node] =
    <button type="button" class="btn btn-outline-dark btn-sm" onclick={(e: Event) =>
      active.value_=(true)
      activeModule = Option(d)}>
      Change space
    </button>

  @dom
  def activeDisplay(o: Octopus)(implicit d: ActionMove): Binding[Node] =
    <button type="button" class="btn btn-outline-dark btn-sm btn-active" onclick={(e: Event) =>
      changeParent(o)
      active.value_=(false)}>
      Move here
    </button>

  def changeParent(o: Octopus) = {
    activeModule foreach { a => a.moveParent(o) }
    activeModule = None
  }
}

trait ControlDisplayWithState[Dependant] {
  protected val active: Var[Boolean] = Var(false)

  protected def inactiveDisplay(o: Octopus)(implicit d: Dependant): Binding[Node]

  protected def activeDisplay(o: Octopus)(implicit d: Dependant): Binding[Node]

  protected def findDependant(o: Octopus): Option[Dependant]

  @dom
  def displayAny(o: Octopus): Binding[Node] = findDependant(o) map { implicit d ⇒
    if (active.bind) activeDisplay(o).bind else inactiveDisplay(o).bind
  } getOrElse DisplayModel.nospan.bind

  @dom
  def displayActiveOnly(o: Octopus): Binding[Node] = if (active.bind) {
    findDependant(o) map { implicit d ⇒ activeDisplay(o).bind } getOrElse DisplayModel.nospan.bind
  } else DisplayModel.nospan.bind

  @dom
  def displayInactiveOnly(o: Octopus): Binding[Node] = if (!active.bind) {
    findDependant(o) map { implicit d ⇒ inactiveDisplay(o).bind } getOrElse DisplayModel.nospan.bind
  } else DisplayModel.nospan.bind

}
