package life.plenty.ui.display

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.{Answer, Contribution, Proposal, User}
import life.plenty.model.octopi.definition.Hub
import life.plenty.model.utils.GraphUtils
import life.plenty.ui.display.utils.Helpers.{BindableHtmlProperty, OptBindableHtmlProperty, OptBindableHub, OptBindableProperty}
import life.plenty.ui.model.{DisplayModule, ExclusiveModuleOverride, ModuleOverride, SimpleDisplayModule}
import org.scalajs.dom.Node
import rx.{Ctx, Rx}

package object feed {
  trait FeedDisplay[T <: Hub] extends SimpleDisplayModule[T] {
    @dom
    protected def actionHtml(a: String): Binding[Node] = <p class="action-text">{a}</p>
  }

  trait FeedDisplaySimple[T <: Hub] extends FeedDisplay[T] {

    protected val action: Rx[String]
    protected def actionTarget(implicit hub:T, ctx: Ctx.Owner): Rx[String]
    protected val cssClass: String

    @dom
    private def actionTargetHtml(at: String): Binding[Node] = <p class="action-target-text">{at}</p>

    @dom
    override def html(hub: T): Binding[Node] = {
      implicit val c = hub.ctx
      val parent = GraphUtils.getParent(hub)
      val atb = new BindableHtmlProperty(actionTarget(hub, c), actionTargetHtml)
      val ab = new BindableHtmlProperty(action, actionHtml)

      <div class={"feed " + cssClass} id={hub.id}>
        {SimpleDisplayModule.html(FullUserBadge, hub.getCreator).bind} {ab.dom.bind} {atb.dom.bind}
      </div>
    }

  }
}
