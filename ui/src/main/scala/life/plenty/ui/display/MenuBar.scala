package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.Space
import life.plenty.ui.display.meta.NoDisplay
import life.plenty.ui.model.DisplayModel.{DisplayModule, ModuleOverride}
import org.scalajs.dom.raw.Node

class MenuBar(override val withinOctopus: Space) extends DisplayModule[Space] with TitleDisplay {
  //  private val wallet = Var(getWallet)

  override def overrides: List[ModuleOverride] = super.overrides ::: List(
    ModuleOverride(this, new NoDisplay(withinOctopus), (m) ⇒ m.isInstanceOf[MenuBar]))

  override def update(): Unit = {
    super.update()
    //    wallet.value_=(getWallet)
  }

  //  private def getWallet = withinOctopus.getTopModule({ case m: CurrentUserWallet ⇒ m })
  //  private def getWallet = withinOctopus.getTopModule({ case m: CurrentUserWallet ⇒ m })

  @dom
  protected override def generateHtml(overrides: List[ModuleOverride]): Binding[Node] = {
    <div class="menu-bar d-flex flex-row justify-content-between align-items-center">
      <div>back</div>
      <h3 class="title">
        {Var(withinOctopus.title).bind}
      </h3>
      <div class="wallet">
        {CurrentUserWallet.generateHtml(withinOctopus).bind}
      </div>
    </div>
  }
}

//{wallet.bind match {
//  case Some(w) => w.getHtml.bind
//  case None => <span>no wallet found</span>
//}}
