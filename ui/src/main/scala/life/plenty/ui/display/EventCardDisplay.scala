package life.plenty.ui.display

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.{Event, User}
import life.plenty.ui.display.actions.{ChangeParent, EditSpace}
import life.plenty.ui.display.meta.{ChildDisplay, NoDisplay}
import life.plenty.ui.model.DisplayModel.DisplayModule
import life.plenty.ui.model.Helpers._
import life.plenty.ui.model._
import org.scalajs.dom.raw.{MouseEvent, Node}
import rx.Rx

class EventCardDisplay(override val withinOctopus: Event) extends DisplayModule[Event] {
  override def update(): Unit = Unit

  override def doDisplay(): Boolean = UiContext.startingSpace.value.get.id != withinOctopus.id

  private def navigateTo(e: MouseEvent) = Router.navigateToOctopus(withinOctopus)

  private lazy val editor: BindableAction[EditSpace] = new BindableAction(withinOctopus.getTopModule({ case
    m: EditSpace ⇒ m
  }), this)

  @dom
  override protected def generateHtml(): Binding[Node] = {
    <div class="card d-inline-flex mt-1 mr-1 flex-column event">
      <div class="d-inline-flex flex-row flex-nowrap">

        <div class="d-inline-flex flex-column controls">
          <button type="button" class="btn btn-primary btn-sm" onclick={navigateTo _}>Explore</button>
        </div>

        <div class="card-body" onclick={navigateTo _}>
          <h6 class="card-title">Event:
            {withinOctopus.getTitle.dom.bind}
          </h6>
          <h6 class="card-subtitle mb-2 text-muted">by
            {val c: Rx[Option[String]] = withinOctopus.getCreator.map((optU: Option[User]) => optU.map {
            u: User => u.getNameOrEmpty(): String
          });
          c.dom.bind}
          </h6>
          <p class="card-text">
            Look for details inside
          </p>
        </div>
      </div>

      <div class="card-controls-bottom d-flex">
        {ChangeParent.displayActiveOnly(withinOctopus).bind}{editor.dom.bind}
      </div>
    </div>
  }

  override def overrides: List[ModuleOverride] = {
    SimpleModuleOverride(this, new NoDisplay(withinOctopus), dm ⇒ dm.isInstanceOf[ChildDisplay]) :: super.overrides
  }
}
