package life.plenty.ui.display
import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.Space
import life.plenty.model.actions.ActionCreateQuestion
import life.plenty.ui.model.DisplayModel.{DisplayModule, ModuleOverride, TitleDisplay}
import org.scalajs.dom.html.Input
import org.scalajs.dom.raw.{KeyboardEvent, Node}

import scalaz.std.list._


class TitleWithNav(override val withinOctopus: Space) extends DisplayModule[Space] with TitleDisplay {
  override def overrides: List[ModuleOverride] = super.overrides ::: List(
    ModuleOverride(new NoDisplay(withinOctopus), (m) ⇒ m.isInstanceOf[TitleWithNav]))

  @dom
  override def displaySelf(overrides: List[ModuleOverride]): Binding[Node] = {
    <div class="nav-bar">
      <div>back</div>
      <div class="title">
        {Var(withinOctopus.title).bind}
      </div>
    </div>
  }
}

class TitleWithQuestionInput(override val withinOctopus: Space) extends DisplayModule[Space] with TitleDisplay {
  @dom
  override def displaySelf(overrides: List[ModuleOverride]): Binding[Node] = {
    val action = withinOctopus.getTopModule { case m: ActionCreateQuestion ⇒ m }

    <div class="title-with-input">
      <div class="title">
        {Var(withinOctopus.title).bind}
      </div>
      <input type="text" disabled={action.isEmpty} onkeyup={onEnter _}/>
    </div>
  }
  private def onEnter(e: KeyboardEvent) = {
    if (e.keyCode == 13) {
      withinOctopus.getTopModule { case m: ActionCreateQuestion ⇒ m } foreach (a ⇒ {
        a.create(e.srcElement.asInstanceOf[Input].value)
      })
    }
  }
}