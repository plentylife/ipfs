package life.plenty.ui.display

import scala.concurrent.ExecutionContext.Implicits.global
import com.thoughtworks.binding.Binding.Vars
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.connection.Parent
import life.plenty.model.hub._
import life.plenty.model.hub.definition.Hub
import life.plenty.model.utils.{GraphExtractorsDEP, GraphUtils}
import life.plenty.ui
import life.plenty.ui.display.feed.SpaceFeedDisplay
import life.plenty.ui.display.utils.DomRenderListSingleModule
import life.plenty.ui.display.utils.Helpers._
import life.plenty.ui.model._
import org.scalajs.dom.raw.Node
import rx.Rx
import scalaz.std.list._
import scalaz.std.option._

import scala.concurrent.Future

class UserLayout(override val hub: User) extends DisplayModule[User] {

  // todo don't forget about root space filter

  override def doDisplay(): Boolean = UiContext.pointer.value.exists(_.id == hub.id)

  def getMemberships: Future[List[Hub]] = {
    val ms = hub.whenLoadComplete map { _ ⇒ hub.sc.exAll({ case Parent(m: Members) ⇒ m })}
    ms flatMap { membersList ⇒
      val parentalList = membersList map { m ⇒
        m.loadCompletedHub map {_.sc.ex({ case Parent(p: Hub) ⇒ p })}
      }
      Future.sequence(parentalList) map {_ collect { case Some(p) ⇒ p }}
    }
  }

  def getTopMemberships: Future[List[Space]] = {
    val ms = getMemberships
    println(s"GTM $ms")
    ms flatMap { list ⇒
      println(s"GTM c $list")
      val spaces = list.collect({ case h: Space ⇒ h })
      val withParents = spaces.map(
        s ⇒ GraphUtils.hasParentInChain(s, spaces filterNot {_ == s}) map {s → _}
      )
      Future.sequence(withParents) map {_.collect{case (s, false) ⇒ s}}
    }
  }

  private lazy val membershipsList = Vars[Space]()

  @dom
  override protected def generateHtml(): Binding[Node] = {
    val titleClasses = "title ml-2 "

    <div class={"top-space-layout user-feed"}>
      <div class="layout-header">
        <h3 class={titleClasses}>
          {GraphExtractorsDEP.getName(hub).dom.bind}
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
      }}
      </div>

    </div>
  }

  override def update(): Unit = {
    membershipsList.value.clear()
    getTopMemberships map {list =>
      println(s"TOP MEMBERSHIPS $list")
      membershipsList.value.insertAll(0, list)
    }
  }
}