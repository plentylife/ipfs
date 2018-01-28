package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.actions.ActionCreateQuestion
import life.plenty.model.connection.Parent
import life.plenty.model.octopi.{GreatQuestion, Octopus, Space}
import life.plenty.ui.model.DisplayModel.{DisplayModule, ModuleOverride}
import life.plenty.ui.model.Helpers._
import org.scalajs.dom.html.Input
import org.scalajs.dom.raw.{KeyboardEvent, Node}
import rx.{Ctx, Rx}

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
        {withinOctopus.getTitle.dom.bind}
      </h3>
      <span class="d-inline-flex">
        <input type="text" disabled={action.isEmpty} onkeyup={onEnter _} placeholder="ask your question"/>
        ?
      </span>
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

  //  implicit val parser: (String) ⇒ String = {(s) ⇒ prefix + s}
  //      {Var(prefix + withinOctopus.title.dom).bind}{"?"}

  @dom
  override protected def generateHtml(overrides: List[ModuleOverride]): Binding[Node] = {
    //println("question title display")
    <div class="question-title">
      {gqTitle.dom.bind}{withinOctopus.getTitle.dom.bind}{"?"}
    </div>
  }

  val prefix: Var[String] = Var("")

  implicit val ctx: Ctx.Owner = Ctx.Owner.safe()

  private val gqParent = {withinOctopus.rx.get({ case Parent(p: GreatQuestion) ⇒ p })}
  private val gqTitle: Rx[Option[String]] = gqParent.flatMap {
    case Some(gq) ⇒ gq.getTitle: Rx[Option[String]]
    case None ⇒ rx.Var(Some("")): Rx[Option[String]]
  }

  gqTitle.foreach(t ⇒ println(s"title changed ${t}"))



  //  window.setTimeout(() ⇒ {println("question.title", withinOctopus.connections)}, 3000)

  //    private val o = withinOctopus.getAllTopConnectionDataRx({ case Parent(p: GreatQuestion) ⇒ p }).foreach({
  //      case Some(p) ⇒ println(s"prefix ${p.title()}"); prefix.value_=(p.title() + " ")
  //      case _ ⇒ println("no prefix", withinOctopus.connections); ""
  //    })

}