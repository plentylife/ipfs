package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Vars
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.data.DbReaderModule
import life.plenty.model.octopi.{Members, User}
import life.plenty.ui
import life.plenty.ui.display.actions.AnswerControls
import life.plenty.ui.display.meta.LayoutModule
import life.plenty.ui.display.utils.CardNavigation
import life.plenty.ui.model.DisplayModel.DisplayModule
import life.plenty.ui.display.utils.Helpers.{BasicBindable, OptBindableProperty}
import life.plenty.ui.model.{DisplayModel, UiContext}
import org.scalajs.dom.Event
import org.scalajs.dom.raw.Node
import rx.Obs

import scalaz.std.list._

class MembersCardDisplay(override val hub: Members) extends DisplayModule[Members] with CardNavigation {
  private var addedCurrentUser = false

  private lazy val members: BasicBindable[List[User]] = hub.getMembers

  override def update(): Unit = {
    // fixme this will need to go
    if (!addedCurrentUser) {
      DbReaderModule.onFinishLoad(hub, () ⇒ {
        ui.console.trace(s"Trying to add member to space with modules ${hub.modules}")
        hub.addMember(UiContext.userVar.value)
        addedCurrentUser = true
      })
    }
  }

  @dom
  override protected def generateHtml(): Binding[Node] = {
    <div class="card d-inline-flex members" id={hub.id}>
      <span class="card-controls">
        <div class="btn-help d-inline-flex" onclick={_: Event ⇒ Help.membersCardHelp}>help</div>
        <div class="btn btn-primary btn-sm open-btn" onclick={navigateTo _}>open</div>
      </span>
      <div class="card-body">
          {for (m <- members().bind) yield displayMember(m).bind}
      </div>
    </div>
  }

  private def displayMember(u: User): Binding[Node] = DisplayModel.display(u, Nil, Option(this))
}

