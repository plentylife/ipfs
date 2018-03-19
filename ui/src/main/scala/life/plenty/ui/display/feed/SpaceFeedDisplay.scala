package life.plenty.ui.display.feed

import com.thoughtworks.binding.Binding.{Var, Vars}
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.connection.{Child, Marker, Title}
import life.plenty.model.hub.Space
import life.plenty.model.hub.definition.Hub
import life.plenty.ui.display.actions.OpenButton
import life.plenty.ui.display.utils.{DomBinders, DomStream}
import life.plenty.ui.model._
import org.scalajs.dom.{Event, Node}
import life.plenty.model.utils.ObservableGraphUtils._
import monix.execution.Scheduler.Implicits.global

object SpaceFeedDisplay extends SimpleDisplayModule[Space] {
  def fits(what: Any) = what.isInstanceOf[Space]

  @dom
  def html(hub: Space): Binding[Node] = {
    println(s"CREATED SPACEFEED $hub")

    val aggregated = collectDownTree[Hub](hub, matchBy = {
      case Child(h: Hub) ⇒ h
      case m: Marker ⇒ m
    },allowedPath = {case Child(h: Hub) ⇒ h})

    //    val aggregatedB: ListBindable[Binding[Node]] = new ListBindable(aggregated map {
    //      list ⇒
    //        val additional = VoteGroup.groupByAnswer(list)
    //        val fullList = list ::: additional()
    //        fullList flatMap {h: Any ⇒
    //          FeedModuleDirectory get h map {m ⇒ m.html(h)}
    //        } : List[Binding[Node]]
    //    })

    val cssClass = ""

    <div class={"card d-inline-flex flex-column space " + cssClass} id={hub.id}>
      <span class="d-flex header-block">
        <span class="d-flex title-block" onclick={e: Event ⇒ Router.navigateToHub(hub)}>
          <h5 class="card-title">
            {DomBinders.text(hub conEx { case Title(t) => t }).bind}
          </h5>
        </span>

        <span class="card-controls">
          {OpenButton.html(hub).bind}
        </span>
      </span>

      <div class="card-body">
        {for (dh <- new DomStream(aggregated).v) yield {
        FeedModuleDirectory.get(dh).map {
          m => m.html(dh)
        } getOrElse DisplayModel.nospan
      }.bind}

      </div>

    </div>
  }

}
