package life.plenty.ui.display.actions

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.{ContainerSpace, Question, Space}
import life.plenty.ui.model.{DisplayModel, UiContext}
import life.plenty.ui.model.DisplayModel.{ActionDisplay, DisplayModule}
import org.scalajs.dom.{Event, Node}
import life.plenty.ui.model.Helpers._

import scalaz.std.option._

class SpaceActionsBar(override val withinOctopus: Space) extends DisplayModule[Space] {
  override def update(): Unit = Unit

  private lazy val ask = new BindableModule(withinOctopus.getTopModule({case m: CreateQuestionButton ⇒ m}), this)
  private lazy val answer = new BindableModule(withinOctopus.getTopModule({case m: CreateAnswerButton ⇒ m}), this)

  @dom
  override protected def generateHtml(): Binding[Node] = {
    <div class="d-inline-flex space-actions-bar">
      <div class="btn btn-primary" onclick={onCreateSpace _}>create a space</div>
      {ask.dom.bind}
      {answer.dom.bind}
      {if (UiContext.startingSpace.bind.exists{_.id != withinOctopus.id} && withinOctopus.isInstanceOf[ContainerSpace])
        <div class="btn btn-primary">answer</div>
    else DisplayModel.nospan.bind
      }
    </div>
  }

  private def onCreateSpace(e: Event) = {
    CreateSpace.openInModal(withinOctopus)
  }
}

//{if(!withinOctopus.isInstanceOf[Question])
//  <div class="btn btn-primary" onclick={onCreateSpace _}>create a space</div> else DisplayModel.nospan.bind}