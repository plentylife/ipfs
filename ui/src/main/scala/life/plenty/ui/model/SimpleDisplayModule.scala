package life.plenty.ui.model

import com.thoughtworks.binding.Binding
import org.scalajs.dom.Node

trait SimpleDisplayModule {
  def html: Binding[Node]
}
