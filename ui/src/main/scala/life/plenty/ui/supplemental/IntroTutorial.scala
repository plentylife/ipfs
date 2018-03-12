package life.plenty.ui.supplemental

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.User
import life.plenty.ui.display.Modal
import org.scalajs.dom.Node

object IntroTutorial {
  def apply(user: User): Unit = {
    Modal.giveContentAndOpen(content, "tutorial-modal-box")
  }

  @dom
  private def content: Binding[Node] = {
    <div>
      the tutorial should appear here
    </div>
  }
}
