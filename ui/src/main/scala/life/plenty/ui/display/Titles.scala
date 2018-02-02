package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.actions.ActionCreateQuestion
import life.plenty.model.connection.Parent
import life.plenty.model.octopi.{GreatQuestion, Octopus, Space}
import life.plenty.ui
import life.plenty.ui.display.actions.EditQuestion
import life.plenty.ui.model.DisplayModel.DisplayModule
import life.plenty.ui.model.Helpers._
import org.scalajs.dom.html.Input
import org.scalajs.dom.raw.{KeyboardEvent, Node}
import rx.Rx

import scalaz.std.list._

trait TitleDisplay extends DisplayModule[Octopus] {
  /*todo. make global title var and make updater */
  override def update(): Unit = Unit
}

class TitleWithQuestionInput(override val withinOctopus: Space) extends DisplayModule[Space] with TitleDisplay {

  @dom
  protected override def generateHtml(): Binding[Node] = {
    //println("title with inputt", withinOctopus.modules)
    val action = withinOctopus.getTopModule { case m: ActionCreateQuestion ⇒ m }

    <div class="title-with-input d-flex mt-3">
      <h4 class="title mr-3">
        {withinOctopus.getTitle.dom.bind}
      </h4>
      <span class="d-inline-flex">
        <input type="text" disabled={action.isEmpty} onkeyup={onEnter _}
               placeholder="ask your question and hit `enter`"/>
        ?
      </span>
    </div>
  }

  private def onEnter(e: KeyboardEvent) = {
    if (e.keyCode == 13) {
      withinOctopus.getTopModule { case m: ActionCreateQuestion ⇒ m } foreach (a ⇒ {
        a.create(e.target.asInstanceOf[Input].value)
      })
    }
  }
}

class QuestionTitle(override val withinOctopus: Space) extends DisplayModule[Space] with TitleDisplay {
  override def doDisplay(): Boolean = !withinOctopus.modules.exists(_.isInstanceOf[TitleWithQuestionInput])

  private lazy val editor: BindableModule[EditQuestion] = withinOctopus.getTopModule({ case m: EditQuestion ⇒ m })

  @dom
  override protected def generateHtml(): Binding[Node] = {
    ui.console.println(s"question title display ${editor.module}")
    <div class="question-title">

      {editor.dom.bind}<span class={if (!editor.active.bind) "" else "d-none"}>
      {gqTitle.dom.bind}{withinOctopus.getTitle.dom.bind}
      ?
    </span>


    </div>
  }

  val prefix: Var[String] = Var("")

  // must be lazy since class is not instantiated at time of load
  private lazy val gqParent = {withinOctopus.rx.get({ case Parent(p: GreatQuestion) ⇒ p })}
  private lazy val gqTitle: Rx[Option[String]] = gqParent.flatMap {
    case Some(gq) ⇒ gq.getTitle: Rx[Option[String]]
    case None ⇒ rx.Var(Some("")): Rx[Option[String]]
  }

}