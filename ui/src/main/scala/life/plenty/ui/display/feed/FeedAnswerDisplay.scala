package life.plenty.ui.display.feed

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.{Answer, Contribution, Proposal}
import life.plenty.ui.display.utils.CardNavigation
import life.plenty.ui.display.utils.Helpers._
import life.plenty.ui.model.DisplayModule
import org.scalajs.dom.Node
import scalaz.std.list._
import scalaz.std.option._

class FeedAnswerDisplay(override val hub: Answer) extends DisplayModule[Answer] with CardNavigation with FeedDisplay {
  override def doDisplay() = true

  private val creator = new OptBindableHub(hub.getCreator, this)
  private lazy val parent = hub.getParent
  private lazy val action = if (hub.isInstanceOf[Proposal]) "proposed"
  else if (hub.isInstanceOf[Contribution]) "contributed" else "answered"

  @dom
  override protected def generateHtml(): Binding[Node] = {
    <div class={"feed answer"} id={hub.id}>
      {creator.dom.bind} {action}
    </div>
  }
  override def update(): Unit = Unit
}
