package life.plenty.ui.supplemental

import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.Node

object ErrorModals {
  @dom
  def noSuchUserFound: Binding[Node] = {
    <div>
      There is no such user. Check your password and email.
    </div>
  }
}
