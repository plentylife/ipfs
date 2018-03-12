package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.ui.model.{DisplayModel, UiContext}
import org.scalajs.dom.html.Input
import org.scalajs.dom.{Event, Node}

import scala.collection.immutable.Queue

object Modal {

  private val isOpen = Var(false)
  private val content = Var[Binding[Node]](null)
  private val outerClasses = Var("")
  private val defualtClasses = "modal-outer-box d-flex justify-content-center align-items-center "
  private val closeButtonText = Var("close")
  private val hasCloseButton = Var(true)

  private var contentQueue = Queue[ContentInfo]()

  private case class ContentInfo(content: Binding[Node], cssClass: String, closeBtnText: String,
                                 hasCloseButton: Boolean)

  def giveContentAndOpen(c: Binding[Node], addClass: String = "", closeBtnText: String = "close",
                         hasCloseButton: Boolean = true) = {
    contentQueue = contentQueue.enqueue(ContentInfo(c, addClass, closeBtnText, hasCloseButton))
    setContent()
  }

  private def setContent() = {
    if (!isOpen.value) {
      contentQueue.dequeueOption match {
        case Some((content, queue)) ⇒
          contentQueue = queue
          outerClasses.value_=(content.cssClass)
          this.closeButtonText.value_=(content.closeBtnText)
          this.hasCloseButton.value_=(content.hasCloseButton)
          this.content.value_=(content.content)
          isOpen.value_=(true)
        case _ ⇒
          content.value_=(null)
          outerClasses.value_=("")
      }
    }
  }

  def close() = {
    isOpen.value_=(false)
    setContent()
  }

  @dom
  def display(): Binding[Node] = {
    if (isOpen.bind) {
      <div class={defualtClasses + outerClasses.bind}>
        {content.bind.bind //
        }
        {if (hasCloseButton.bind) { //
        <div class="btn-sm btn btn-danger mt-3" onclick={_: Event => close()}>
          {closeButtonText.bind}
        </div>
      } else DisplayModel.nospan.bind }

      </div>
    } else {
      <span style="display:none"></span>
    }
  }
}

object ErrorModal {
  def setContentAndOpen(c: Binding[Node]): Unit = {
    Modal.giveContentAndOpen(withHeader(c), "error", "close")
  }

  @dom
  private def withHeader(c: Binding[Node]): Binding[Node] = {
    <span>
      <h5 class="error-header">Nope, you can't do that...</h5>{c.bind}
    </span>
  }
}

