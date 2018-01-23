package life.plenty.ui.display
import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.actions.ActionCreateQuestion
import life.plenty.model.connection.Parent
import life.plenty.model.octopi.{GreatQuestion, Octopus, Space}
import life.plenty.ui.model.DisplayModel.{DisplayModule, ModuleOverride}
import org.scalajs.dom.html.Input
import org.scalajs.dom.raw.{KeyboardEvent, Node}

import scalaz.std.list._


trait TitleDisplay extends DisplayModule[Octopus] {
  /*todo. make global title var and make updater */
  override def update(): Unit = Unit
}

class TitleWithQuestionInput(override val withinOctopus: Space) extends DisplayModule[Space] with TitleDisplay {

  @dom
  protected override def generateHtml(overrides: List[ModuleOverride]): Binding[Node] = {
    //println("title with inputt", withinOctopus.modules)
    val action = withinOctopus.getTopModule { case m: ActionCreateQuestion ⇒ m }

    <div class="title-with-input d-flex mt-3">
      <h3 class="title mr-3">
        {Var(withinOctopus.title()).bind}
      </h3>
      <input type="text" disabled={action.isEmpty} onkeyup={onEnter _} placeholder="ask your question"/>
    </div>
  }

  private def onEnter(e: KeyboardEvent) = {
    if (e.keyCode == 13) {
      withinOctopus.getTopModule { case m: ActionCreateQuestion ⇒ m } foreach (a ⇒ {
        a.create(e.srcElement.asInstanceOf[Input].value)
      })
    }
  }
}

class QuestionTitle(override val withinOctopus: Space) extends DisplayModule[Space] with TitleDisplay {
  override def doDisplay(): Boolean = !withinOctopus.modules.exists(_.isInstanceOf[TitleWithQuestionInput])

  @dom
  override protected def generateHtml(overrides: List[ModuleOverride]): Binding[Node] = {
    //println("question title display")
    <div class="question-title">
      {Var(prefix + withinOctopus.title()).bind}{"?"}
    </div>
  }
  private def prefix = withinOctopus.getTopConnectionData({ case Parent(p: GreatQuestion) ⇒ p }) match {
    case Some(p) ⇒ p.title() + " "
    case _ ⇒ ""
  }
}