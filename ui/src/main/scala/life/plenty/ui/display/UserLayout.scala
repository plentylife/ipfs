package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.connection.Parent
import life.plenty.model.hub._
import life.plenty.model.hub.definition.Hub
import life.plenty.model.utils.GraphEx._
import life.plenty.model.utils.GraphUtils
import life.plenty.ui.display.feed.SpaceFeedDisplay
import life.plenty.ui.display.utils.FutureDom._
import life.plenty.ui.display.utils.{FutureList, FutureOptVar}
import life.plenty.ui.model._
import org.scalajs.dom.raw.Node
import scalaz.std.list._
import scalaz.std.option._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object UserLayout extends SimpleDisplayModule[User] {
  def getMemberships(hub: Hub): Future[List[Hub]] = {
    val ms = hub.whenLoadComplete map { _ ⇒ hub.sc.exAll({ case Parent(m: Members) ⇒ m }) }
    ms flatMap { membersList ⇒
      val parentalList = membersList map { m ⇒
        m.loadCompletedHub map {_.sc.ex({ case Parent(p: Hub) ⇒ p })}
      }
      Future.sequence(parentalList) map {_ collect { case Some(p) ⇒ p }}
    }
  }

  def getTopMemberships(hub: Hub): Future[List[Space]] = {
    val ms = getMemberships(hub)
    println(s"GTM $ms")
    ms flatMap { list ⇒
      println(s"GTM c $list")
      val spaces = list.collect({ case h: Space ⇒ h })
      val withParents = spaces.map(
        s ⇒ GraphUtils.hasParentInChain(s, spaces filterNot {_ == s}) map {s → _}
      )
      Future.sequence(withParents) map {_.collect { case (s, false) ⇒ s }}
    }
  }

  @dom
  override def html(what: User): Binding[Node] = {
    val membershipsList = new FutureList[Space](getTopMemberships(what))
    val membershipsRenders = for (m <- membershipsList.v) yield SpaceFeedDisplay.html(m).bind

    val titleClasses = "title ml-2 "
    <div class={"top-space-layout user-feed"}>
      <div class="layout-header">
        <h3 class={titleClasses}>
          {new FutureOptVar(getName(what)).bind}
        </h3>
        <h5 class="sub-title mt-1 ml-2 text-muted">
          User feed (since last login)
        </h5>
      </div>

      <div class="user-feed">
        {membershipsRenders.bind}
      </div>

    </div>
  }

  override def fits(what: Any): Boolean = what.isInstanceOf[User]
}

class UserLayout(override val hub: User) extends DisplayModule[User] {
  override def doDisplay(): Boolean = UiContext.pointer.value.exists(_.id == hub.id)
  override protected def generateHtml(): Binding[Node] = {
    UserLayout.html(hub)
  }
  override def update(): Unit = {
    Unit
  }
}