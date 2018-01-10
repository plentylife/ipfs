package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.Answer
import life.plenty.ui.model.DisplayModel
import life.plenty.ui.model.DisplayModel.{DisplayModule, ModuleOverride}
import org.scalajs.dom.raw.Node

class AnswerDisplay(override val withinOctopus: Answer) extends DisplayModule[Answer] {
  protected val body = Var[String](withinOctopus.body)

  override def update(): Unit = body.value_=(withinOctopus.body)

  @dom
  override protected def generateHtml(overrides: List[DisplayModel.ModuleOverride]): Binding[Node] = {
    <div>
      this is an answer with a body
      {body.bind}
    </div>
  }

}

//class BasicAnswerDisplay(override val withinOctopus: Answer) extends AnswerDisplay(withinOctopus) {
//
//}


class ContributionDisplay(override val withinOctopus: Answer) extends AnswerDisplay(withinOctopus) {
  override def overrides: List[DisplayModel.ModuleOverride] =
    ModuleOverride(new NoDisplay(withinOctopus),
      m ⇒ m.isInstanceOf[AnswerDisplay] && !m.isInstanceOf[ContributionDisplay]) :: super.overrides

  @dom
  override def generateHtml(overrides: List[DisplayModel.ModuleOverride]): Binding[Node] = {
    <div>
      this is a contribution with a body
      {body.bind}
    </div>
  }
}