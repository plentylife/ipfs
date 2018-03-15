package life.plenty.ui.display.actions

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.hub.Space
import life.plenty.ui.display.utils.Helpers._
import life.plenty.ui.model.DisplayModule
import org.scalajs.dom.{Event, Node}
import scalaz.std.option._

class SpaceActionsBar(override val hub: Space) extends DisplayModule[Space] {
  override def update(): Unit = Unit

  private lazy val ask = new BindableModule(hub.getTopModule({ case m: CreateQuestionButton ⇒ m }), this)
  private lazy val answer = new BindableModule(hub.getTopModule({ case m: CreateAnswerButton ⇒ m }), this)
  private lazy val signup = new BindableModule(hub.getTopModule({ case m: SignupButton ⇒ m }), this)

  @dom
  override protected def generateHtml(): Binding[Node] = {
    <div class="d-inline-flex space-actions-bar">
      <div class="btn btn-primary" onclick={onCreateSpace _}>create a space</div>{ask.dom.bind}{answer.dom
      .bind}{signup.dom.bind}
    </div>
  }

  private def onCreateSpace(e: Event) = {
    CreateSpace.openInModal(hub)
  }
}