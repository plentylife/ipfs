package life.plenty.ui.display

import java.util.Date

import com.thoughtworks.binding.Binding.{BindingSeq, Var}
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.hub._
import life.plenty.model.hub.definition.Hub
import life.plenty.model.utils.GraphUtils
import life.plenty.model.utils.GraphExtractors
import life.plenty.ui
import life.plenty.ui.display.actions.SpaceActionsBar
import life.plenty.ui.display.feed.SpaceFeedDisplay
import life.plenty.ui.display.meta.LayoutModule
import life.plenty.ui.display.utils.Helpers._
import life.plenty.ui.model._
import org.scalajs.dom.raw.Node
import rx.Rx
import scalaz.std.list._
import scalaz.std.option._

import scala.scalajs.js
import scala.scalajs.js.timers.SetTimeoutHandle


class UserLayout(override val hub: User) extends LayoutModule[User] {

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
    ms().filterNot { h =>
      val in = GraphUtils.hasParentInChain(h, ms() filterNot {_ == h})
      in()
    }
  }

  private lazy val membershipsList = new ListBindable(getTopMemberships map {
    _ flatMap SpaceFeedDisplay.htmlOpt
  })

  override def overrides = List(ExclusiveModuleOverride(m ⇒ m.isInstanceOf[TopSpaceLayout] || m
    .isInstanceOf[CardSpaceDisplay] ), ExclusiveModuleOverride(m ⇒ m.isInstanceOf[UserLayout] ))

  @dom
  override protected def generateHtml(): Binding[Node] = {
    val cos: Seq[ModuleOverride] = this.cachedOverrides.bind
    implicit val os = cos.toList ::: siblingOverrides ::: overrides
    val titleClasses = "title ml-2 "

    <div class={"top-space-layout user-feed"}>
      <div class="layout-header">
        <h3 class={titleClasses}>
          {GraphExtractors.getName(hub).dom.bind}
        </h3>
      </div>

      <div class="user-feed">
        {try {
          for (m <- membershipsList()) yield m.bind
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
}