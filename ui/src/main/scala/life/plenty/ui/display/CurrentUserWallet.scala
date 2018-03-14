package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.Space
import life.plenty.model.octopi.definition.Hub
import life.plenty.model.octopi.pseudo.Wallet
import life.plenty.ui
import life.plenty.ui.display.utils.Helpers._
import life.plenty.ui.display.utils.Helpers.intToStr
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
  def generateHtml(withinOctopus: Hub): Binding[Node] = {
    update(withinOctopus)
    wallet.bind match {
      case None => <div class="current-user-wallet-outer-box">Wallet cannot be displayed</div>
      case Some(w) => displayWallet(w).bind
    }
  }

  def update(withinOctopus: Hub): Unit = {
    withinOctopus match {
      case h: Space ⇒ wallet.value_=(Option(new Wallet(UiContext.getUser, h)))
      case _ ⇒ wallet.value_=(None)
    }
  }

  @dom
  private def displayThanks(t: Int) = {
    val pos = t >= 0
    <span>
      <span class={if (pos) "positive" else "negative"}>
        {if (pos) "+" else "—"}
      </span><span>{Math.abs(t)}{ui.thanks}</span>
    </span>
  }

  @dom
  private def displayWallet(w: Wallet): Binding[Node] = {
    <div class="current-user-wallet-outer-box d-inline-flex">
      <div class="d-inline-flex flex-column align-items-center">
        <div class="balance thanks-balance">
          {displayThanks((w.getThanksBalance: BasicBindable[Int])().bind ).bind}
        </div>
        <div class="credit-limit">
          {w.getUsableThanksLimit.dom.bind}{ui.thanks}
        </div>
        <div class="text-muted info-text d-inline-flex flex-column align-items-center" onclick={e: Event ⇒ Help
          .walletBalanceHelp}>
          <span class="balance-text">Balance <br/>(
            <span class="negative">
              {BindableProperty(w.getThanksSpoilRate)(sp => "—" + Math.round(sp * 100).toString + "%").dom.bind}
            </span>
            <span>/day)</span>
          </span>
          <span>Credit Limit</span>
          <img class="question-tooltip" src="iconic/svg/question-mark.svg" alt="help"/>
        </div>
      </div>
      <div class="d-inline-flex flex-column align-items-center mt-3">
        <div class="balance">
          {BindableProperty(w.getUsableVotes).dom.bind}
        </div>
        <div class="text-muted info-text d-inline-flex flex-column align-items-center"
             onclick={e: Event ⇒ Help.voteBalanceHelp}>
          Voting Power
          <img class="question-tooltip" src="iconic/svg/question-mark.svg" alt="help"/>
        </div>
      </div>
    </div>
  }
}

//<div class="text-muted info-text">Spoiling at
//{BindableProperty(w.getThanksSpoilRate)(sp => Math.round(sp * 100).toString).dom.bind}
//% per day</div>
