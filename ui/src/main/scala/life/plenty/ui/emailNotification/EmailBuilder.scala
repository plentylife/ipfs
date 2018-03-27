package life.plenty.ui.emailNotification

import com.thoughtworks.binding.Binding.{BindingSeq, Vars}
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.hub.User
import life.plenty.ui.display.UserLayout
import life.plenty.ui.display.feed.SpaceFeedDisplay
import org.scalajs.dom.Node
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

object EmailBuilder {

  /** gives a flat map of all events in all spaces */
  def getToRender(user: User) = {
    val mems = UserLayout.getTopMemberships(user)
    val memsWithFeed = mems flatMap {list ⇒
      val ags = list map {m ⇒ SpaceFeedDisplay.getAggregated(m) map {m → _}}
      Future.sequence(ags)
    }

    memsWithFeed map {
      _ filterNot (_._2.isEmpty) flatMap {_._2}
    }
  }

  def renderings(user: User): Future[List[Binding[Node]]] = {
    getToRender(user) map {list ⇒
      list map SpaceFeedDisplay.display
    }
  }
}
