package life.plenty.ui.display

import java.util.Date

import com.thoughtworks.binding.Binding.{BindingSeq, Var, Vars}
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.connection.{DataHub, Parent}
import life.plenty.model.hub._
import life.plenty.model.hub.definition.Hub
import life.plenty.model.utils.GraphUtils
import life.plenty.model.utils.GraphExtractors
import life.plenty.ui
import life.plenty.ui.display.actions.SpaceActionsBar
import life.plenty.ui.display.feed.SpaceFeedDisplay
import life.plenty.ui.display.meta.LayoutModule
import life.plenty.ui.display.utils.{DomList, DomListSingleModule}
import life.plenty.ui.display.utils.Helpers._
import life.plenty.ui.model._
import org.scalajs.dom.raw.Node
import rx.Rx
import scalaz.std.list._
import scalaz.std.option._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.timers.SetTimeoutHandle


class UserLayout(override val hub: User) extends DisplayModule[User] {

  // todo don't forget about root space filter

  override def doDisplay(): Boolean = UiContext.pointer.value.exists(_.id == hub.id)

  def getMemberships: Future[List[Space]] = GraphExtractors.getMemberships(hub) flatMap { m ⇒
      val ps = m map {_.conEx {case Parent(p: Space) ⇒ p}}
      Future.sequence(ps).map(_.flatten)
    }

  def getTopMemberships: Future[List[Space]] =
    getMemberships flatMap {ms ⇒
      val inChain: List[Future[(Space, Boolean)]] = ms map { h ⇒
        GraphUtils.hasParentInChain(h, ms filterNot {_ == h}) map {h → _}
      }
      Future.sequence(inChain)
    } map {ms ⇒
      ms.filterNot(_._2).map(_._1)
    }

  lazy val membershipsList = Vars[Space]()

  getTopMemberships foreach {l ⇒
    membershipsList.value.insertAll(0, l)
  }

  @dom
  override protected def generateHtml(): Binding[Node] = {
    println("Generating UserLayout")

    val titleClasses = "title ml-2 "

    <div class={"top-space-layout user-feed"}>
      <div class="layout-header">
        <h3 class={titleClasses}>
          {GraphExtractors.getName(hub).dom.bind}
        </h3>
      </div>

      <div class="user-feed">
        {try {
         for (m <- membershipsList) yield SpaceFeedDisplay.html(m).bind
      } catch {
        case e: Throwable =>
          ui.console.error("Failed during render inside UserLayout")
          ui.console.error(e)
          throw e;
      } }
      </div>

    </div>
  }

//  @dom
// todo if empty

//  protected def sections(implicit overrides: List[ModuleOverride]): List[Binding[Node]] =
//    displayHubs(membershipsList(), "administrative section") :: Nil
  override def update(): Unit = Unit
}