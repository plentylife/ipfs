package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.GraphUtils
import life.plenty.model.connection.Child
import life.plenty.model.octopi.{Members, Octopus, Wallet}
import life.plenty.ui
import life.plenty.ui.UiContext
import org.scalajs.dom.raw.Node

object CurrentUserWallet {
  private val wallet = Var[Option[Wallet]](None)
  private val thanksBalance = Var[Int](0)
  private val thanksLimit = Var[Int](0)
  private val thanksSpoilRate = Var[Double](0)
  private val voteBalance = Var[Int](0)

  @dom
  def generateHtml(withinOctopus: Octopus): Binding[Node] = {
    update(withinOctopus)
    wallet.bind match {
      case None => <div class="current-user-wallet-outer-box">you are not part of this group</div>
      case Some(w) => displayWallet().bind
    }
  }

  def update(withinOctopus: Octopus): Unit = {
    wallet.value_=(findWallet(withinOctopus))
    wallet.value.foreach { w ⇒
      thanksBalance.value_=(w.getUsableThanksAmount)
      thanksLimit.value_=(w.getUsableThanksLimit)
      thanksSpoilRate.value_=(w.getThanksSpoilRate)
      voteBalance.value_=(w.getUsableVotes)
    }
  }

  private def findWallet(withinOctopus: Octopus): Option[Wallet] = {
    //    println("trying to find wallet in ", withinOctopus, withinOctopus.connections)
    GraphUtils.findModuleUpParentTree(withinOctopus, { case Child(m: Members) ⇒ m }).flatMap(m ⇒ {
      m.members.find(_ == UiContext.getUser).flatMap(u ⇒ {
        u.getTopConnectionData({
          case Child(w: Wallet) ⇒ w
        })
      })
    })
  }

  @dom
  private def displayWallet(): Binding[Node] = {
    <div class="current-user-wallet-outer-box d-inline-flex flex-row mr-2">
      <div class="d-inline-flex flex-column align-items-center mr-2">
        <div class="balance">
          {thanksBalance.bind.toString + ui.thanks}
          /
          {thanksLimit.bind.toString + ui.thanks}
        </div>
        <div class="text-muted">Balance / Credit Limit</div>
        <div class="text-muted">Spoiling at
          {Math.round(thanksSpoilRate.bind * 100).toString}
          % per day</div>
      </div>
      <div class="d-inline-flex flex-column align-items-center">
        <div class="balance">
          {voteBalance.bind.toString}
        </div>
        <div class="text-muted">Voting Power</div>
      </div>
    </div>
  }
}
