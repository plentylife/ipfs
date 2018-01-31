package life.plenty.ui.display

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.{Event, User}
import life.plenty.ui.display.meta.{ChildDisplay, NoDisplay}
import life.plenty.ui.model.DisplayModel.{DisplayModule, ModuleOverride}
import life.plenty.ui.model.Helpers._
import life.plenty.ui.model.{DisplayModel, UiContext}
import org.scalajs.dom.raw.{MouseEvent, Node}
import rx.{Ctx, Rx}

class EventCardDisplay(override val withinOctopus: Event) extends DisplayModule[Event] {
  implicit val ctx: Ctx.Owner = Ctx.Owner.safe()

  override def update(): Unit = Unit

  override def doDisplay(): Boolean = UiContext.startingSpace.value.get.id != withinOctopus.id

  private def navigateTo(e: MouseEvent) = ???

  @dom
  override protected def generateHtml(overrides: List[DisplayModel.ModuleOverride]): Binding[Node] = {
    <div class="card d-inline-flex mt-1 mr-1 flex-row event">
      <div class="d-inline-flex flex-column controls">
        <button type="button" class="btn btn-primary btn-sm" onclick={navigateTo _}>Explore</button>
      </div>

      <div class="card-body">
        <h6 class="card-title">event</h6>
        <h6 class="card-subtitle mb-2 text-muted">by
          {val c: Rx[Option[String]] = withinOctopus.getCreator.map((optU: Option[User]) => optU.map {
          u: User => u.getNameOrEmpty(): String
        });
        c.dom.bind}
        </h6>
        <p class="card-text">
          {withinOctopus.getTitle.dom.bind}
        </p>
      </div>
    </div>
  }

  override def overrides: List[DisplayModel.ModuleOverride] = {
    ModuleOverride(this, new NoDisplay(withinOctopus), dm ⇒ dm.isInstanceOf[ChildDisplay]) :: super.overrides
  }
}
