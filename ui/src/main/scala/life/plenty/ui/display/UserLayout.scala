package life.plenty.ui.display

import java.util.Date

import com.thoughtworks.binding.Binding.{BindingSeq, Var, Vars}
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.connection.DataHub
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
import scala.scalajs.js
import scala.scalajs.js.timers.SetTimeoutHandle


class UserLayout(override val hub: User) extends DisplayModule[User] {

  // todo don't forget about root space filter

  override def doDisplay(): Boolean = UiContext.pointer.value.exists(_.id == hub.id)

  def getMemberships = Rx {
    val ms = GraphExtractors.getMemberships(hub)
    ms().flatMap {m ⇒
      val p = GraphExtractors.getParent(m)
      p()
    }
  }



  def getTopMemberships = Rx {
    val ms = getMemberships
    ms().collect({case h: Space ⇒ h}).filterNot { h =>
      val in = GraphUtils.hasParentInChain(h, ms() filterNot {_ == h})
      in()
    }
  }

  lazy val membershipsList = Vars[Binding[Node]]()
  def loadAll(h: Hub, depth: Int): Unit = {
    if (!h.loadComplete.isCompleted && depth < 4) {
      println(s"LOADING $h ${h.onConnectionsRequest}")
      h.onConnectionsRequest.foreach(f ⇒ f())
      h.loadComplete.future foreach {_ ⇒
        println(s"LOADING COMPLETE $h ${h.sc.all}")
        h.sc.all.foreach {
          case d: DataHub[_] if d.value.isInstanceOf[Hub] && !d.value.isInstanceOf[User] ⇒
            println(d → d.value)
            loadAll(d.value.asInstanceOf[Hub], depth + 1)
          case _ ⇒
        }
      }
    }
  }

  loadAll(hub, 0)



//  private lazy val membershipsList = new DomListSingleModule[Space](getMemberships map {
//    _ collect {case h: Space ⇒ h}
//  }, SpaceFeedDisplay)
//  private lazy val membershipsList = new DomListSingleModule[Space](getTopMemberships map {
//  _ ⇒ List()
//}, SpaceFeedDisplay)
//  private lazy val membershipsList = new DomListSingleModule[Space](Rx{List()}, SpaceFeedDisplay)

  override def overrides = List(ExclusiveModuleOverride(m ⇒ m.isInstanceOf[TopSpaceLayout] || m
    .isInstanceOf[CardSpaceDisplay] ), ExclusiveModuleOverride(m ⇒ m.isInstanceOf[UserLayout] ))


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
         for (m <- membershipsList) yield m.bind
//        ""
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