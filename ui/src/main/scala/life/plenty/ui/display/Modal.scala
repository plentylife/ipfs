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

  def setContentAndOpen(c: Binding[Node], addClass: String = "") = {
    outerClasses.value_=(addClass)
    content.value_=(c)
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
      </div>
    } else {
      <span style="display:none"></span>
    }
  }
}
