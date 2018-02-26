package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Vars
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.data.DbReaderModule
import life.plenty.model.octopi.{Members, User}
import life.plenty.model.utils.GraphUtils
import life.plenty.ui
import life.plenty.ui.display.actions.AnswerControls
import life.plenty.ui.display.meta.LayoutModule
import life.plenty.ui.display.utils.CardNavigation
import life.plenty.ui.model.DisplayModel.DisplayModule
import life.plenty.ui.display.utils.Helpers.{BasicBindable, BindableModule, OptBindableProperty}
import life.plenty.ui.model.{ComplexModuleOverride, DisplayModel, ModuleOverride, UiContext}
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
        ui.console.trace(s"Trying to add member ${UiContext.userVar.value} to $hub")
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

  private val udo = new ComplexModuleOverride(this, {case m: BadgeMemberEarned ⇒ m}, _.isInstanceOf[FullUserBadge])
//  private val udo = new ModuleOverride(this, {case m: BadgeMemberEarned ⇒ m}, _.isInstanceOf[FullUserBadge])
  private def displayMember(u: User): Binding[Node] = DisplayModel.display(u, List(udo), Option(this))
}

import life.plenty.ui.display.utils.Helpers._
class BadgeMemberEarned(override val hub: User) extends DisplayModule[User] {
  override def update(): Unit = Unit

  private lazy val badgeDisplay = new BindableModule(hub.getTopModule({case m: FullUserBadge ⇒ m}), this)

  private lazy val contributions = GraphUtils.getAllContributionsInSpace(UiContext.startingSpace.value.get, hub)
  private lazy val contributionsCount = contributions.map(_.size)

  @dom
  override protected def generateHtml(): Binding[Node] = {
    <div class="d-flex user-earned-box">
      <div>
      {badgeDisplay.dom.bind} earned
      </div>
      <div>
        {contributionsCount.dom.bind} contributions
      </div>
    </div>
  }
}