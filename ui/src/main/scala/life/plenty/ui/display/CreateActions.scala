package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.Question
import life.plenty.model.actions.ActionCreateAnswer
import life.plenty.ui.model.DisplayModel
import life.plenty.ui.model.DisplayModel.DisplayModule
import org.scalajs.dom.Event
import org.scalajs.dom.html.{Input, TextArea}
import org.scalajs.dom.raw.Node

class CreateAnswer(override val withinOctopus: Question) extends DisplayModule[Question] {
  println("Create answer ", withinOctopus)
  private lazy val action = Var(false)
  private val opened = Var(false)
  private val isContribution = Var(false)

  override def doDisplay() = findAction.nonEmpty
  //  override def doDisplay() = true
  private def findAction: Option[ActionCreateAnswer] = withinOctopus.getTopModule({ case m: ActionCreateAnswer ⇒ m })
  override def update(): Unit = {
    action.value_=(findAction.nonEmpty)
  }
  @dom
  override protected def generateHtml(overrides: List[DisplayModel.ModuleOverride]): Binding[Node] = {
    if (!opened.bind) {
      <div class={"closed-answer-box"}>
        {newAnswerButton.bind}
      </div>
    } else {
      <div class={"open-answer-box"}>
        <label for="is-contribution">Can someone contribute this?</label>
        <input type="checkbox" name="is-contribution" checked={isContribution.bind} onchange={toggleContribution _}/>
        <textarea cols={50} rows={10}>opened</textarea>{postAnswerButton.bind}
      </div>
    }
  }
  @dom
  private def newAnswerButton: Binding[Node] = {
      <input type="button" value={"+answer"} disabled={!action.bind} onclick={e: Event ⇒
      opened.value_=(true);}/>
  }
  @dom
  private def postAnswerButton: Binding[Node] = {
      <input type="button" value="post answer" onclick={postAnswer _}/>
  }

  private def toggleContribution(e: Event) = {
    val cbox = e.srcElement.asInstanceOf[Input].checked
    isContribution.value_=(cbox)
  }
  private def postAnswer(e: Event) = {
    val input = e.srcElement.asInstanceOf[TextArea].value
    findAction foreach { a ⇒
      a.create(input)
    }
  }
}

