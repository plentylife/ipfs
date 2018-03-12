package life.plenty.ui.display.utils

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.ui.display.Modal
import org.scalajs.dom.{Event, Node}

trait ModalFormAction {
  protected def onClick(e: Event): Unit = Modal.giveContentAndOpen(_createDialog())

  @dom
  private def _createDialog(): Binding[Node] = {
    <form class={"d-flex flex-column align-items-center " + formCssClass} onsubmit={_onSubmit _}>
      {createDialog().bind}<input type="submit" class="btn btn-primary" value={formSubmitValue}/>
    </form>
  }

  private def _onSubmit(e: Event): Unit = {
    e.preventDefault()
    onSubmit(e)
  }

  protected val formSubmitValue: String
  protected val formCssClass: String

  protected def onSubmitSuccess(): Unit = Modal.close()

  protected def createDialog(): Binding[Node]

  protected def onSubmit(e: Event): Unit
}
