package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.definition.Hub
import life.plenty.model.octopi.pseudo.Wallet
import life.plenty.model.octopi.{User, WithMembers}
import life.plenty.ui
import life.plenty.ui.model.DisplayModel.intToStr
import life.plenty.ui.display.utils.Helpers._
import life.plenty.ui.model.UiContext
import org.scalajs.dom.Event
import org.scalajs.dom.raw.Node
import rx.{Ctx, Obs}

object CurrentUserWallet {
  private implicit val ctx = Ctx.Owner.safe()
  private val wallet = Var[Option[Wallet]](None)
  private var walletObs: Obs = null

  private val thanksBalance = Var[Int](0)
  private val thanksLimit = Var[Int](0)
  private val thanksSpoilRate = Var[Double](0)
  private val voteBalance = Var[Int](0)

  @dom
  def generateHtml(withinOctopus: Hub): Binding[Node] = withinOctopus match {
    case o: WithMembers ⇒
      update(o)
      wallet.bind match {
        case None => <div class="current-user-wallet-outer-box">you are not part of this group</div>
        case Some(w) => displayWallet(w).bind
      }
    case _ ⇒ <div>Wallet cannot be displayed</div>
  }

  def update(withinOctopus: WithMembers): Unit = {
    if (walletObs == null) {
      walletObs = withinOctopus.getMembers.foreach(_.foreach(_.getMembers.foreach {
        list ⇒
          val w = list.collectFirst({ case u: User if u.id == UiContext.getUser.id ⇒ new Wallet(u, withinOctopus) })
          if (w.nonEmpty) wallet.value_=(w)
      }))
    }
  }

  @dom
  private def displayWallet(w: Wallet): Binding[Node] = {
    <div class="current-user-wallet-outer-box d-inline-flex">
      <div class="d-inline-flex flex-column align-items-center">
        <div class="balance" data:data-toggle="popover" data:data-content="Disabled popover">
          {w.getUsableThanksAmount.dom.bind}{ui.thanks}
          /
          {w.getUsableThanksLimit.dom.bind}{ui.thanks}
        </div>
        <div class="text-muted" onclick={e: Event ⇒ Help.walletBalanceHelp}>
          Balance / Credit Limit
          <img class="question-tooltip" src="iconic/svg/question-mark.svg" alt="help"/>
        </div>
        <div class="text-muted">Spoiling at
          {BindableProperty(w.getThanksSpoilRate)(sp => Math.round(sp * 100).toString).dom.bind}
          % per day</div>
      </div>
      <div class="d-inline-flex flex-column align-items-center">
        <div class="balance">
          {BindableProperty(w.getUsableVotes).dom.bind}
        </div>
        <div class="text-muted" onclick={e: Event ⇒ Help.voteBalanceHelp}>Voting Power
          <img class="question-tooltip" src="iconic/svg/question-mark.svg" alt="help"/>
        </div>
      </div>
    </div>
  }
}
