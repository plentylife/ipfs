package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.Question
import life.plenty.ui.model.DisplayModel
import life.plenty.ui.model.DisplayModel.DisplayModule
import org.scalajs.dom.Event
import org.scalajs.dom.raw.Node

class CreateAnswer(override val withinOctopus: Question) extends DisplayModule[Question] {
  private val opened = Var(false)
  override protected def updateSelf(): Unit = Unit
  @dom
  override protected def generateHtml(overrides: List[DisplayModel.ModuleOverride]): Binding[Node] = {
    println("affects", affects)
    <div>
      {if (!opened.value) {
      button.bind
    } else {
      <div>
        {"opened"}
      </div>
    }}
    </div>
  }
  @dom
  private def button: Binding[Node] = {
      <input type="button" value="+answer" onclick={e: Event ⇒
      println("e");
      opened.value_=(true);
      println(opened
        .value);
      update}/>
  }
}

