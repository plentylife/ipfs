package life.plenty.ui.display.cards

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.hub.{Event, User}
import life.plenty.ui.display.actions.{ChangeParent, EditSpace}
import life.plenty.ui.display.meta.{ChildDisplay, NoDisplay}
import life.plenty.ui.display.utils.Helpers._
import life.plenty.ui.model.{DisplayModule, _}
import org.scalajs.dom.raw.{MouseEvent, Node}
import rx.Rx

@deprecated("don't use for now. might come back to life")
class EventCardDisplay(override val hub: Event) extends DisplayModule[Event] {
  override def update(): Unit = Unit

  override def doDisplay(): Boolean = UiContext.pointer.value.get.id != hub.id

  private def navigateTo(e: MouseEvent) = Router.navigateToHub(hub)

  private lazy val editor: BindableAction[EditSpace] = new BindableAction(hub.getTopModule({ case
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
            {hub.getTitle.dom.bind}
          </h6>
          <h6 class="card-subtitle mb-2 text-muted">by
            {val c: Rx[Option[String]] = hub.getCreator.map((optU: Option[User]) => optU.map {
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
        {ChangeParent.displayActiveOnly(hub).bind}{editor.dom.bind}
      </div>
    </div>
  }

  override def overrides: List[ModuleOverride] = {
    SimpleModuleOverride(this, new NoDisplay(hub), dm ⇒ dm.isInstanceOf[ChildDisplay]) :: super.overrides
  }
}
