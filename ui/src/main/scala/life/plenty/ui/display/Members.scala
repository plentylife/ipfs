package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Vars
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.data.OctopusGunReaderModule
import life.plenty.model.octopi.{Members, User}
import life.plenty.ui
import life.plenty.ui.display.meta.LayoutModule
import life.plenty.ui.model.DisplayModel.DisplayModule
import life.plenty.ui.model.Helpers.{BasicBindable, OptBindableProperty}
import life.plenty.ui.model.{DisplayModel, UiContext}
import org.scalajs.dom.raw.Node
import rx.Obs
import scalaz.std.list._

class MembersCardDisplay(override val withinOctopus: Members) extends LayoutModule[Members] {
  private var addedCurrentUser = false

  private lazy val members: BasicBindable[List[User]] = withinOctopus.getMembers

  override def update(): Unit = {
    // fixme this will need to go
    if (!addedCurrentUser) {
      OctopusGunReaderModule.onFinishLoad(withinOctopus, () ⇒ {
        ui.console.trace(s"Trying to add member to space with modules ${withinOctopus.modules}")
        withinOctopus.addMember(UiContext.userVar.value)
        addedCurrentUser = true
      })
    }
  }

  @dom
  override protected def generateHtml(): Binding[Node] = {
    <div class="card d-inline-flex members" id={withinOctopus.id}>
      <h3 class="card-title">Members</h3>

      <div class="card-body">
        <ul>
          {for (m <- members().bind) yield displayMember(m).bind}
        </ul>
      </div>
    </div>
  }

  @dom
  private def displayMember(u: User): Binding[Node] = <li id={u.id}>
    {DisplayModel.display(u, Nil, Option(this)).bind}
  </li>
}

