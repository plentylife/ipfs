package life.plenty.ui.display.feed

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.connection.{Child, Marker}
import life.plenty.model.hub.Space
import life.plenty.model.hub.definition.Hub
import life.plenty.model.hub.pseudo.VoteGroup
import life.plenty.model.utils.GraphUtils.collectDownTree
import life.plenty.model.utils.GraphExtractors
import life.plenty.ui.display.actions.OpenButton
import life.plenty.ui.display.meta.LayoutModule
import life.plenty.ui.display.utils.CardNavigation
import life.plenty.ui.display.utils.Helpers._
import life.plenty.ui.display.{CardQuestionDisplayBase, FullUserBadge, InlineQuestionDisplay}
import life.plenty.ui.model._
import org.scalajs.dom.{Event, Node}
import rx.async._
import rx.async.Platform._

import scala.concurrent.duration._

object SpaceFeedDisplay extends SimpleDisplayModule[Space] {
  def fits(what: Any) = what.isInstanceOf[Space]

  @dom
  def html(hub: Space): Binding[Node] = {
    implicit val ctx = hub.ctx
    val aggregated = collectDownTree[Hub](hub, matchBy = {
      case Child(h: Hub) ⇒ h
      case m: Marker ⇒ m
    },allowedPath = {case Child(h: Hub) ⇒ h}).debounce(1000 milliseconds)

    val aggregatedB: ListBindable[Binding[Node]] = new ListBindable(aggregated map {
      list ⇒
        val additional = VoteGroup.groupByAnswer(list)
        val fullList = list ::: additional()
        fullList flatMap {h: Any ⇒
          FeedModuleDirectory get h map {m ⇒ m.html(h)}
        } : List[Binding[Node]]
    })

    val cssClass = ""

    <div class={"card d-inline-flex flex-column space " + cssClass} id={hub.id}>
      <span class="d-flex header-block">
        <span class="d-flex title-block" onclick={e: Event ⇒ Router.navigateToHub(hub)}>
          <h5 class="card-title">
            {hub.getTitle.dom.bind}
          </h5>
        </span>

        <span class="card-controls">
          {OpenButton.html(hub).bind}
        </span>
      </span>

      <div class="card-body">
        {for (b <- aggregatedB()) yield {b.bind}}
      </div>

    </div>
  }
}
