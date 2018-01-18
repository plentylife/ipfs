package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.Node

object Help {
  private val open = Var(false)
  private val text = Var("")
  // hack
  private var closeTriggered: Int = 0

  def triggerClose() = {
    if (closeTriggered == 1) {
      open.value_=(false)
      closeTriggered = 0
    } else closeTriggered += 1
  }

  @dom
  def display(): Binding[Node] = {
    if (open.bind) {
      <div class="main-help">
        {text.bind}
      </div>
    } else {
      <span style="display:none"></span>
    }
  }

  def walletBalanceHelp = displayNow("wallet balance")

  /* Predefined helps */

  private def displayNow(msg: String) = {
    text.value_=(msg)
    open.value_=(true)
  }

  def voteBalanceHelp = displayNow("vote balance")
}
