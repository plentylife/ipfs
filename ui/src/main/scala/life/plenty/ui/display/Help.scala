package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.ui
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

  private def displayNow(msg: String) = {
    text.value_=(msg)
    open.value_=(true)
  }

  /* Predefined helps */

  def walletBalanceHelp = displayNow("This is your account balance. It is represented in the internal currency -- " +
    "Thanks -- gratitude, which you give to people who make contributions to your common projects and events. Unlike " +
    "regular money that you are used to, it spoils (expires), so 'if you don't use it, you lose it'.")

  def voteBalanceHelp = displayNow("This represents the amount of power you have to steer a group and to prioritize " +
    "tasks. To gain votes, you must give Thanks to people who have made contributions.")

  def membersCardHelp = displayNow(
    s"""
      |this dislpays how many ${ui.thanks}hanks members have earned in this space and
      |        sub-spaces. Details on user contributions are inside.
    """.stripMargin)
}
