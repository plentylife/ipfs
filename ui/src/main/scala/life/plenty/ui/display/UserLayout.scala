package life.plenty.ui.display

import com.thoughtworks.binding.Binding.{BindingSeq, Var}
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi._
import life.plenty.model.octopi.definition.Hub
import life.plenty.model.utils.GraphUtils
import life.plenty.ui.display.actions.SpaceActionsBar
import life.plenty.ui.display.meta.LayoutModule
import life.plenty.ui.display.utils.Helpers._
import life.plenty.ui.model.{DisplayModel, ModuleOverride, UiContext}
import org.scalajs.dom.raw.Node
import rx.Rx
import scalaz.std.list._
import scalaz.std.option._


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
    ms().filterNot { h =>
      val in = GraphUtils.hasParentInChain(h, ms() filterNot {_ == h})
      in()
    }
  }

  private lazy val membershipsList = new ListBindable(getTopMemberships)

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

      <div class="user-feed">
        {for (s <- sections) yield s.bind}
      </div>
    </div>
  }

  protected def sections(implicit overrides: List[ModuleOverride]): List[Binding[Node]] =
    displayHubs(membershipsList(), "administrative section") :: Nil
}