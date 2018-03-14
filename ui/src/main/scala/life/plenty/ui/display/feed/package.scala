package life.plenty.ui.display

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.{Answer, Contribution, Proposal}
import life.plenty.model.octopi.definition.Hub
import life.plenty.model.utils.GraphUtils
import life.plenty.ui.display.utils.Helpers.{BindableHtmlProperty, OptBindableHtmlProperty, OptBindableHub, OptBindableProperty}
import life.plenty.ui.model.{DisplayModule, ExclusiveModuleOverride, ModuleOverride}
import org.scalajs.dom.Node
import rx.Rx

package object feed {
  trait FeedDisplay[T <: Hub] extends DisplayModule[T] {
    override def doDisplay() = true
    override def update(): Unit = Unit

    @dom
    protected def actionHtml(a: String): Binding[Node] = <p class="action-text">{a}</p>
  }

  trait FeedDisplaySimple[T <: Hub] extends FeedDisplay[T] {

    private lazy val parent = GraphUtils.getParent(hub)
    protected val action: Rx[String]
    protected val actionTarget: Rx[String]
    protected val cssClass: String

    @dom
    private def actionTargetHtml(at: String): Binding[Node] = <p class="action-target-text">{at}</p>

    @dom
    override protected def generateHtml(): Binding[Node] = {
      val atb = new BindableHtmlProperty(actionTarget, actionTargetHtml)
      val ab = new BindableHtmlProperty(action, actionHtml)
      val os = ExclusiveModuleOverride(_.isInstanceOf[FeedDisplay[_]]) :: overrides ::: cachedOverrides.bind.toList
      println(s"FEED DISP SIMPLe over ${this} ${cachedOverrides.bind} ${lastCaller}")
      <div class={"feed " + cssClass} id={hub.id}>
        {new OptBindableHub(hub.getCreator, this, os).dom.bind} {ab.dom.bind} {atb.dom.bind}
      </div>
    }

  }
}
