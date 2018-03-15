package life.plenty.ui.display.actions

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.actions.ActionMove
import life.plenty.model.hub.definition.Hub
import life.plenty.ui.model.DisplayModel
import org.scalajs.dom.Event
import org.scalajs.dom.raw.Node

import scalaz.std.option._

object ChangeParent extends ControlDisplayWithState[ActionMove] {

  protected override def findDependant(o: Hub) =
    o.getTopModule({ case a: ActionMove ⇒ a })

  private var activeModule: Option[ActionMove] = None

  @dom
  def inactiveDisplay(o: Hub)(implicit d: ActionMove): Binding[Node] = {
    <button type="button" class="btn btn-outline-dark btn-sm" onclick={(e: Event) =>
      activeModule = Option(d)
      active.value_=(true)}>
      Change space
    </button>
  }

  @dom
  def activeDisplay(o: Hub)(implicit d: ActionMove): Binding[Node] =
    <div class="d-inline-flex">
      {
    if (!activeModule.exists(_.hub.id == o.id)) {
      <button type="button" class="btn btn-outline-dark btn-sm btn-active" onclick={(e: Event) =>
        changeParent(o)
        active.value_=(false)}>
        Move here
      </button>
    } else {
      <button type="button" class="btn btn-warning btn-sm" onclick={(e: Event) =>
        cancelMove}>
        Cancel move
      </button>
    }}
    </div>

  def cancelMove = {
    activeModule = None
    active.value_=(false)
    onComplete()
  }

  def changeParent(o: Hub) = {
    activeModule foreach { a => a.moveParent(o) }
    activeModule = None
    onComplete()
  }
}

trait ControlDisplayWithState[Dependant] {
  protected val active: Var[Boolean] = Var(false)
  protected var listeners: Set[() ⇒ Unit] = Set()

  protected def inactiveDisplay(o: Hub)(implicit d: Dependant): Binding[Node]

  protected def activeDisplay(o: Hub)(implicit d: Dependant): Binding[Node]

  protected def findDependant(o: Hub): Option[Dependant]

  protected def onComplete(): Unit = {
    listeners foreach { f ⇒ f() }
    //    listeners = Set()
  }

  @dom
  def displayAny(o: Hub): Binding[Node] = findDependant(o) map { implicit d ⇒
    if (active.bind) activeDisplay(o).bind else inactiveDisplay(o).bind
  } getOrElse DisplayModel.nospan.bind

  @dom
  def displayActiveOnly(o: Hub): Binding[Node] = if (active.bind) {
    findDependant(o) map { implicit d ⇒ activeDisplay(o).bind } getOrElse DisplayModel.nospan.bind
  } else DisplayModel.nospan.bind

  @dom
  def displayInactiveOnly(o: Hub, listener: Option[() ⇒ Unit] = None): Binding[Node] = if (!active.bind) {
    findDependant(o) map { implicit d ⇒
      listener foreach { l ⇒ listeners += l }
      inactiveDisplay(o).bind
    } getOrElse DisplayModel.nospan.bind
  } else DisplayModel.nospan.bind

}
