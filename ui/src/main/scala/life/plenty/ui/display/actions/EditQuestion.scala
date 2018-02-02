package life.plenty.ui.display.actions

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.Question
import life.plenty.ui.model.DisplayModel.{DisplayModule, Insertable}
import org.scalajs.dom.raw.Node

class EditQuestion(override val withinOctopus: Question) extends DisplayModule[Question] with Insertable {
  override def update(): Unit = Unit

  @dom
  override protected def generateHtml(): Binding[Node] = if (!active.bind) {
    <div class="d-inline-flex">"edit q"</div>
  } else {
    <div class="d-inline-flex">"editing..."</div>
  }


}
