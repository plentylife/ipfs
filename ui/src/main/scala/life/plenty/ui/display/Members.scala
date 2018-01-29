package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Vars
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.actions.ActionAddMember
import life.plenty.model.octopi.{Members, User}
import life.plenty.ui
import life.plenty.ui.model.DisplayModel.DisplayModule
import life.plenty.ui.model.{DisplayModel, UiContext}
import org.scalajs.dom.raw.Node
import rx.{Ctx, Obs}

class MembersDisplay(override val withinOctopus: Members) extends DisplayModule[Members] {
  implicit val ctx: Ctx.Owner = Ctx.Owner.safe()

  private val _members = Vars[User]()
  private var membersRx: Obs = null

  private var addedCurrentUser = false

  override def update(): Unit = {
    if (membersRx == null) {
      membersRx = withinOctopus.getMembers.foreach(list ⇒ {
        _members.value.clear()
        _members.value.insertAll(0, list)
        ui.console.println(s"MembersDisplay update ${list}")
      }
      )
    }

    if (!addedCurrentUser) {
      ui.console.println(s"Trying to add member to space with modules ${withinOctopus.modules}")
      o.getTopModule({ case m: ActionAddMember => m }).foreach { m =>
        ui.console.println("module found")
        m.addMember(UiContext.userVar.value)
      }
    }
  }

  @dom
  override protected def generateHtml(overrides: List[DisplayModel.ModuleOverride]): Binding[Node] = {
    <div class="card d-inline-flex mt-2 ml-2">
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
    {u.id}
  </li>
}