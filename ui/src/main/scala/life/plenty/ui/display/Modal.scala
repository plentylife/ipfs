package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.ui.model.UiContext
import org.scalajs.dom.html.Input
import org.scalajs.dom.{Event, Node}

object Modal {

  private val isOpen = Var(false)
  private val content = Var[Binding[Node]](null)
  private val outerClasses = Var("")
  private val defualtClasses = "modal-outer-box d-flex justify-content-center align-items-center "
  private val closeButtonText = Var("close")

  def setContentAndOpen(c: Binding[Node], addClass: String = "", closeBtnText: String = "close") = {
    outerClasses.value_=(addClass)
    content.value_=(c)
    this.closeButtonText.value_=(closeBtnText)
    isOpen.value_=(true)
  }

  def close() = {
    isOpen.value_=(false)
    content.value_=(null)
    outerClasses.value_=("")
  }

  @dom
  def display(): Binding[Node] = {
    if (isOpen.bind) {
      <div class={defualtClasses + outerClasses.bind}>
        {content.bind.bind}
        <div class="btn-sm btn btn-danger mt-3" onclick={_:Event => close()}>{closeButtonText.bind}</div>
      </div>
    } else {
      <span style="display:none"></span>
    }
  }
}

object ErrorModal {
  def setContentAndOpen(c: Binding[Node]): Unit = {
    Modal.setContentAndOpen(withHeader(c), "error", "close")
  }

  @dom
  private def withHeader(c: Binding[Node]): Binding[Node] = {
    <span>
      <h5 class="error-header">Nope, you can't do that...</h5>
      {c.bind}
    </span>
  }
}

object ErrorModals {
  @dom
  def noSuchUserFound: Binding[Node] = {
    <div>
      There is no such user. Check your password and email.
    </div>
  }
}