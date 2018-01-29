package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.{Octopus, User, Wallet, WithMembers}
import life.plenty.ui
import life.plenty.ui.model.Helpers._
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
  def generateHtml(withinOctopus: Octopus): Binding[Node] = withinOctopus match {
    case o: WithMembers ⇒
      update(o)
      wallet.bind match {
        case None => <div class="current-user-wallet-outer-box">you are not part of this group</div>
        case Some(w) => displayWallet(w).bind
      }
    case _ ⇒ <div>Walled display improperly contstructed</div>
  }

  def update(withinOctopus: WithMembers): Unit = {
    if (walletObs == null) {
      walletObs = withinOctopus.getMembers.foreach(_.foreach(_.getMembers.foreach {
        list ⇒
          val w = list.collectFirst({ case u: User if u.id == UiContext.getUser.id ⇒ new Wallet(u) })
          if (w.nonEmpty) wallet.value_=(w)
      }))
    }
  }

  implicit def intToStr(i: Int): String = i.toString

  @dom
  private def displayWallet(w: Wallet): Binding[Node] = {
    <div class="current-user-wallet-outer-box d-inline-flex flex-row mr-2">
      <div class="d-inline-flex flex-column align-items-center mr-2">
        <div class="balance" data:data-toggle="popover" data:data-content="Disabled popover">
          {thanksBalance.bind.toString + ui.thanks}
          /
          {thanksLimit.bind.toString + ui.thanks}
        </div>
        <div class="text-muted" onclick={e: Event ⇒ Help.walletBalanceHelp}>
          Balance / Credit Limit
          <img class="question-tooltip" src="iconic/svg/question-mark.svg" alt="help"/>
        </div>
        <div class="text-muted">Spoiling at
          {Math.round(thanksSpoilRate.bind * 100).toString}
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
