package life.plenty.ui.display.info

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.{Answer, Contribution}
import life.plenty.ui
import life.plenty.ui.display.actions.AnswerControls
import life.plenty.ui.model.DisplayModule
import life.plenty.ui.display.utils.Helpers._
import org.scalajs.dom.Node

trait AnswerInfo extends DisplayModule[Answer]

class ThanksGiven(override val hub: Contribution) extends AnswerInfo {

  override def update(): Unit = Unit

  @dom
  override protected def generateHtml(): Binding[Node] = {
    <span class="d-inline-flex thanks-given">
      {hub.tips.dom.bind}{ui.thanks + " earned"}
    </span>
  }

}