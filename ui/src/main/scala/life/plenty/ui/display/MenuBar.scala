package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.connection.Parent
import life.plenty.model.octopi.Space
import life.plenty.ui
import life.plenty.ui.display.meta.NoDisplay
import life.plenty.ui.model.DisplayModel.{DisplayModule, ModuleOverride}
import life.plenty.ui.model.Helpers._
import life.plenty.ui.model.Router
import org.scalajs.dom.raw.{MouseEvent, Node}
import rx.Obs

class MenuBar(override val withinOctopus: Space) extends DisplayModule[Space] with TitleDisplay {
  override def overrides: List[ModuleOverride] = super.overrides ::: List(
    ModuleOverride(this, new NoDisplay(withinOctopus), (m) ⇒ m.isInstanceOf[MenuBar]))

  private var obs: Obs = null
  private val parentSpace: Var[Option[Space]] = Var(None)

  override def update(): Unit = {
    super.update()
    ui.console.println("MenuBar update")
    if (obs == null) {
      obs = withinOctopus.rx.get({ case Parent(o: Space) ⇒ o }).foreach {
        s ⇒ parentSpace.value_=(s)
      }
    }
  }

  @dom
  private def backBtn: Binding[Node] = if (parentSpace.bind.nonEmpty) {
    <div class="btn btn-sm btn-outline-light navigation-back" onclick={e: MouseEvent ⇒
      Router.navigateToOctopus(parentSpace.value.get)}>back</div>
  } else {<span class="d-none"></span>}

  @dom
  protected override def generateHtml(overrides: List[ModuleOverride]): Binding[Node] = {
    //    println("menu bar display")
    ui.console.println("MenuBar genHtml")
    <div class="menu-bar d-flex flex-row align-items-center">
      {backBtn.bind}
      <h3 class="title ml-2">
        {withinOctopus.getTitle.dom.bind}
      </h3>
      <div class="wallet">
        {CurrentUserWallet.generateHtml(withinOctopus).bind}
      </div>
    </div>
  }
}
