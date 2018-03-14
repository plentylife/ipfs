package life.plenty.ui.display.feed

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.{Answer, Contribution, Proposal}
import life.plenty.ui.display.utils.CardNavigation
import life.plenty.ui.display.utils.Helpers._
import life.plenty.ui.model.DisplayModule
import org.scalajs.dom.Node
import rx.Rx
import scalaz.std.list._
import scalaz.std.option._

class FeedAnswerDisplay(override val hub: Answer) extends FeedDisplay[Answer] with CardNavigation {
  override def doDisplay() = true

  override protected val action = Rx {
    if (hub.isInstanceOf[Proposal]) "proposed"
    else if (hub.isInstanceOf[Contribution]) "contributed" else "answered"
  }

  override protected val actionTarget: Rx[String] = hub.getBody
  override protected val cssClass: String = "answer"
}
