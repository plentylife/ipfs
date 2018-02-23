package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.actions.ActionCreateQuestion
import life.plenty.model.connection.Parent
import life.plenty.model.octopi.definition.Hub
import life.plenty.model.octopi.{GreatQuestion, Space}
import life.plenty.ui
import life.plenty.ui.display.actions.{ChangeParent, EditSpace}
import life.plenty.ui.model.DisplayModel.DisplayModule
import life.plenty.ui.display.utils.Helpers._
import life.plenty.ui.model.Router
import org.scalajs.dom.Event
import org.scalajs.dom.html.Input
import org.scalajs.dom.raw.{KeyboardEvent, Node}
import rx.Rx

import scalaz.std.list._

trait TitleDisplay extends DisplayModule[Hub] {
  /*todo. make global title var and make updater */
  override def update(): Unit = Unit
}

class TitleWithQuestionInput(override val hub: Space) extends DisplayModule[Space] with TitleDisplay {

  private val inputValue = Var("")
  private lazy val action = hub.getTopModule { case m: ActionCreateQuestion ⇒ m }

  @dom
  protected override def generateHtml(): Binding[Node] = {
    //println("title with inputt", withinOctopus.modules)

    <div class="title-with-input d-flex mt-3" id={hub.id}>
      {ChangeParent.displayActiveOnly(hub).bind}<h5 class="title mr-3">
        {hub.getTitle.dom.bind}
    </h5>
      <span class="d-inline-flex">
        <input type="text" disabled={action.isEmpty} onkeyup={onEnter _} value={inputValue.bind}
               placeholder="ask your question and hit `enter`" oninput={e: Event =>
        inputValue.value_=(e.target.asInstanceOf[Input].value)}/>
        <span class="btn btn-sm btn-primary symbolic" onclick={e: Event ⇒ createQuestion}>?</span>
      </span>
    </div>
  }

  private def onEnter(e: KeyboardEvent) =
    if (e.keyCode == 13) createQuestion



  private def createQuestion = {
    action foreach (a ⇒ {
      a.create(inputValue.value, "")
      inputValue.value_=("")
    })
  }
}

class QuestionTitle(override val hub: Space) extends DisplayModule[Space] with TitleDisplay {
  override def doDisplay(): Boolean = !hub.modules.exists(_.isInstanceOf[TitleWithQuestionInput])

  private lazy val editor: BindableAction[EditSpace] = new BindableAction(hub.getTopModule({ case
    m: EditSpace ⇒ m
  }), this)

  // todo add
  //<span class={if (!editor.active.bind) "" else "d-none"}>
  @dom
  override protected def generateHtml(): Binding[Node] = {
    ui.console.println(s"question title display ${editor.module}")
    <div class="question-title" id={hub.id}>
      {ChangeParent.displayActiveOnly(hub).bind}{editor.dom.bind}<span class="title-text" onclick={e: Event
    => Router.navigateToOctopus(hub)}>
      {gqTitle.dom.bind}{hub.getTitle.dom.bind}
      ?
    </span>
    </div>
  }

  val prefix: Var[String] = Var("")

  // must be lazy since class is not instantiated at time of load
  private lazy val gqParent = {hub.rx.get({ case Parent(p: GreatQuestion) ⇒ p })}
  private lazy val gqTitle: Rx[Option[String]] = gqParent.flatMap {
    case Some(gq) ⇒ gq.getTitle: Rx[Option[String]]
    case None ⇒ rx.Var(Some("")): Rx[Option[String]]
  }

}