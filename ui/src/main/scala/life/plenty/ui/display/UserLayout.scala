package life.plenty.ui.display

import java.util.Date

import com.thoughtworks.binding.Binding.{BindingSeq, Var}
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi._
import life.plenty.model.octopi.definition.Hub
import life.plenty.model.utils.GraphUtils
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
    val ms = GraphUtils.getMemberships(hub)
    ms().flatMap {m ⇒
      val p = GraphUtils.getParent(m)
      p()
    }
  }



  def getTopMemberships = Rx {
    val ms = getMemberships
    val r = ms().filterNot { h =>
      val in = GraphUtils.hasParentInChain(h, ms() filterNot {_ == h})
      in()
    }
    lastUpdate = new Date().getTime
    // should cancel previous
    if (timer != null) js.timers.clearTimeout(timer)
    timer = js.timers.setTimeout(1500) {
      if (new Date().getTime - lastUpdate >= 1000) canShow.value_=(true)
      println(s"LAST UPDATE ${new Date().getTime - lastUpdate}")
    }
    r
  }

  private lazy val membershipsList = new ListBindable(getTopMemberships)

  private var lastUpdate = new Date().getTime
  private var timer: SetTimeoutHandle = null
  private lazy val canShow = Var(false)

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
          {GraphUtils.getName(hub).dom.bind}
        </h3>
      </div>

      {if (canShow.bind )
      <div class="user-feed">
        {for (s <- sections) yield s.bind}
      </div> //
    else DisplayModel.nospan.bind
      }

    </div>
  }

  protected def sections(implicit overrides: List[ModuleOverride]): List[Binding[Node]] =
    displayHubs(membershipsList(), "administrative section") :: Nil
}