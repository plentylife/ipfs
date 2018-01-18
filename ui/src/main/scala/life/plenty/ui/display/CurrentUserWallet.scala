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
    {
      wallet.bind match {
        case None => <div class="current-user-wallet-outer-box">you are not part of this group</div>
        case Some(w) => displayWallet(w).bind
      }
    }
  }

  @dom
  private def displayWallet(w: Wallet): Binding[Node] = {
    <div class="current-user-wallet-outer-box d-inline-flex flex-row mr-2">
      <div class="d-inline-flex flex-column align-items-center mr-2">
        <div class="balance">
          {w.getUsableThanksAmount.toString + ui.thanks}
          /
          {w.getUsableThanksLimit.toString + ui.thanks}
        </div>
        <div class="text-muted">Balance / Credit Limit</div>
      </div>
      <div class="d-inline-flex flex-column align-items-center">
        <div class="balance">
          {w.getUsableVotes.toString}
        </div>
        <div class="text-muted">Voting Power</div>
      </div>
    </div>
  }
}
