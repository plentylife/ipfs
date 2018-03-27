package life.plenty.ui.display.feed

import com.thoughtworks.binding.Binding.Vars
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.connection.{Child, Marker}
import life.plenty.model.hub.{Members, Space}
import life.plenty.model.hub.definition.Hub
import life.plenty.model.hub.pseudo.VoteGroup
import life.plenty.model.utils.GraphUtils.collectDownTree
import life.plenty.ui.display.actions.OpenButton
import life.plenty.ui.display.utils.Helpers._
import life.plenty.ui.emailNotification.EmailManager
import life.plenty.ui.model._
import org.scalajs.dom.{Event, Node}

import scala.concurrent.ExecutionContext.Implicits.global

object SpaceFeedDisplay extends SimpleDisplayModule[Space] {
  def fits(what: Any) = what.isInstanceOf[Space]

  def display(what: Any): Binding[Node] = {
    FeedModuleDirectory get what map { m ⇒ m.html(what) } getOrElse DisplayModel.nospan
  }

  def getAggregated(hub: Hub) = {
    collectDownTree[Hub](hub, matchBy = {
      case Child(h: Hub) if !h.isInstanceOf[Members] ⇒ h
      case m: Marker ⇒ m
    }, allowedPath = {case Child(h: Hub) if !h.isInstanceOf[Members] ⇒ h}) flatMap { list ⇒
      val additional = VoteGroup.groupByAnswer(list)
      additional map {list ::: _}
    }
  }

  @dom
  def html(hub: Space): Binding[Node] = {
    val aggregated = getAggregated(hub)

    val bindList = Vars[Object]()
    aggregated.foreach(ags ⇒ bindList.value.insertAll(0, ags))

    val cssClass = if (bindList.bind.isEmpty) "d-none" else ""

    /* fixme still needs marked confirmed */

    <div class={"card flex-column space " + cssClass} id={hub.id}>
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
        {for (h <- bindList) yield display(h).bind}
      </div>

    </div>
  }
}
