package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Vars
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.data.OctopusGunReaderModule
import life.plenty.model.octopi.{Members, User}
import life.plenty.ui
import life.plenty.ui.model.DisplayModel.DisplayModule
import life.plenty.ui.model.Helpers.OptBindableProperty
import life.plenty.ui.model.UiContext
import org.scalajs.dom.raw.Node
import rx.Obs
class MembersDisplay(override val withinOctopus: Members) extends DisplayModule[Members] {

  private val _members = Vars[User]()
  private var membersRx: Obs = null

  private var addedCurrentUser = false

  override def update(): Unit = {
    if (membersRx == null) {
      membersRx = withinOctopus.getMembers.foreach(list ⇒ {
        _members.value.clear()
        _members.value.insertAll(0, list)
        ui.console.trace(s"MembersDisplay update ${list}")
      }
      )
    }

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
    <div class="card d-inline-flex mt-2">
      <div class="card-body">

        <div class="card-title">members of this space:</div>
        <ul>
          {for (m <- _members) yield displayMember(m).bind}
        </ul>
      </div>
    </div>
  }

  @dom
  private def displayMember(u: User): Binding[Node] = <li>
    {u.getName.dom.bind}
  </li>
}