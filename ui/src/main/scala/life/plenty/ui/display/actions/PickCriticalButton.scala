package life.plenty.ui.display.actions

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.actions.ActionAddConfirmedMarker
import life.plenty.model.connection.Child
import life.plenty.model.octopi.{ContainerSpace, Question, Space}
import life.plenty.model.octopi.definition.Hub
import life.plenty.model.utils.GraphUtils
import life.plenty.ui.display.{Controller, InlineDisplay, Modal, TreeView}
import life.plenty.ui.display.actions.labeltraits.MenuAction
import life.plenty.ui.model.ComplexModuleOverride
import life.plenty.ui.model.DisplayModel.{ActionDisplay, DisplayModule}
import org.scalajs.dom.{Event, Node}
import rx.Obs

class PickCriticalButton(override val hub: Hub) extends DisplayModule[Hub] with MenuAction {

  override def update(): Unit = Unit

  @dom
  override protected def generateHtml(): Binding[Node] = <div class="btn btn-outline-primary"
                                                              onclick={e: Event ⇒ open()}>
    pick critical questions
  </div>

  private def open() = {
    val html = TreeView(hub, {
      case Child(s: ContainerSpace) ⇒ s
      case Child(q: Question) ⇒ q
    }, this, List(ComplexModuleOverride(this, {case i: InlineDisplay ⇒ i}, h ⇒ !h.isInstanceOf[InlineDisplay])),
      controllerGenerator)

    println(s"Setting modal content ${html}")
    Modal.giveContentAndOpen(displayModal(html))
  }

  @dom
  private def displayModal(html: Binding[Node]): Binding[Node] = {
    <div>
      <h5>Users trying to join this space will have to answer selected questions first, to be able to join</h5>
      {html.bind}
    </div>
  }

  def controllerGenerator(h: Hub): Controller = {
    new Controller {
      val isSelected =
      override def onClick(e: Event): Unit = Unit
      override def cssClasses: String = ???
      override def prependContent: Binding[Node] = ???
    }
  }
}