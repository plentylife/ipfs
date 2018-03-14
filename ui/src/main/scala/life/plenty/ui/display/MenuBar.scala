package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.connection.Parent
import life.plenty.model.octopi.Space
import life.plenty.model.octopi.definition.Hub
import life.plenty.ui
import life.plenty.ui.display.actions.labeltraits.MenuAction
import life.plenty.ui.display.info.AnswerInfo
import life.plenty.ui.display.meta.{LayoutModule, NoDisplay}
import life.plenty.ui.model.DisplayModule
import life.plenty.ui.display.utils.Helpers._
import life.plenty.ui.model.{ModuleOverride, Router, SimpleModuleOverride}
import org.scalajs.dom.raw.{MouseEvent, Node}
import rx.Obs

class MenuBar(override val hub: Hub) extends LayoutModule[Hub] {
  override def overrides: List[ModuleOverride] = super.overrides ::: List(
    SimpleModuleOverride(this, new NoDisplay(hub), (m) ⇒ m.isInstanceOf[MenuBar]))

  private var obs: Obs = null
  private val parentSpace: Var[Option[Space]] = Var(None)

  override def update(): Unit = {
    super.update()
    ui.console.println("MenuBar update")
    if (obs == null) {
      obs = hub.rx.get({ case Parent(o: Space) ⇒ o }).foreach {
        s ⇒ parentSpace.value_=(s)
      }
    }
  }

  @dom
  private def backBtn: Binding[Node] = if (parentSpace.bind.nonEmpty) {
    <div class="btn btn-outline-light navigation-back" onclick={e: MouseEvent ⇒
      Router.navigateToOctopus(parentSpace.value.get)}>back</div>
  } else {<span class="d-none"></span>}

  @dom
  protected override def generateHtml(): Binding[Node] = {
    ui.console.println("MenuBar genHtml")
    val cos: Seq[ModuleOverride] = this.cachedOverrides.bind
    implicit val os = cos.toList ::: siblingOverrides

    <div class="menu-bar d-flex">
      {backBtn.bind}
      <div class="wallet">
        {CurrentUserWallet.generateHtml(hub).bind}
      </div>

      <div class="actions">
        {displayModules(siblingModules.withFilter(_.isInstanceOf[MenuAction]), "modules").bind}
      </div>
    </div>
  }
}