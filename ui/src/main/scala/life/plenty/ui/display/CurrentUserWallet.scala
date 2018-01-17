package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.GraphUtils
import life.plenty.model.connection.Child
import life.plenty.model.octopi.{Members, Space, Wallet}
import life.plenty.ui
import life.plenty.ui.Context
import life.plenty.ui.model.DisplayModel
import life.plenty.ui.model.DisplayModel.DisplayModule
import org.scalajs.dom.raw.Node

class CurrentUserWallet(override val withinOctopus: Space) extends DisplayModule[Space] {
  private val wallet = Var[Option[Wallet]](None)

  override def doDisplay(): Boolean = false

  def getHtml = {
    update()
    generateHtml(List())
  }

  override def update(): Unit = wallet.value_=(findWallet)

  private def findWallet: Option[Wallet] = {
    GraphUtils.findModuleUpParentTree(withinOctopus, { case Child(m: Members) ⇒ m }).flatMap(m ⇒ {
      m.members.find(_ == Context.getUser).flatMap(u ⇒ {
        u.getTopConnectionData({
          case Child(w: Wallet) ⇒ w
        })
      })
    })
  }

  @dom
  override protected def generateHtml(overrides: List[DisplayModel.ModuleOverride]): Binding[Node] = {
    <div class="current-user-wallet-outer-box">
      {wallet.bind match {
      case None => "you are not part of this group"
      case Some(w) => w.getThanksGivenInSpace.toString + ui.thanks
    }}
    </div>
  }
}
