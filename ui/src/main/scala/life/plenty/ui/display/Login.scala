package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.ui.display.utils.{InputVarWithPassword, StringInputVar}
import life.plenty.ui.model.UiContext
import org.scalajs.dom.html.Input
import org.scalajs.dom.{Event, Node}

import scala.scalajs.js

object Login {
  private val name = Var("")
  private val email = Var(UiContext.getStoredEmail)
  private val password = new StringInputVar()

  private val emailEmpty = Var(false)
  private val nameEmpty = Var(false)

  private val isSignup = Var(false)

  @dom
  private def isOpen: Binding[Boolean] = {
    UiContext.userVar.bind == null
  }

  private def onSubmit(e: Event) = {
    e.preventDefault()
    val nameCondition = !isSignup.value || name.value.nonEmpty
    nameEmpty.value_=(name.value.isEmpty)
    emailEmpty.value_=(email.value.isEmpty)
    if (nameCondition && email.value.nonEmpty) {
      for (pass ← password.get) {

        setInProgress()
        js.timers.setTimeout(100) { // the timer is for giving an opportunity to display
          val nameValue = if (isSignup.value) name.value else null
          UiContext.login(nameValue, email.value, pass)
        }

      }
    }
  }

  private def onSignup(e: Event) = {
    if (isSignup.value) onSubmit(e) else isSignup.value_=(true)
  }

  private val inProgress = Var(false)
  private def setInProgress() = inProgress.value_=(true)
  /* so far only used in case login failed */
  def setFinished() = inProgress.value_=(false)

  @dom
  def display(): Binding[Node] = {
    if (isOpen.bind) {
      <div class="login-outer-box d-flex flex-column justify-content-center align-items-center">
        <div class="d-flex logo">
          <img src="images/plenty_logo-400.png"/>
        </div>
        <form class="login-box d-inline-flex flex-column" onsubmit={e: Event ⇒
          onSubmit(e)}>
          <div class={if (isSignup.bind) "" else "d-none"}>
            {if (nameEmpty.bind) {
            <div class="bg-danger text-white">
              Name can't be empty
            </div>
          } else {
            <span style="display:none"></span>
          }}<label for="name">Your name</label> <br/>
            <input name="name" type="text" autocomplete='name' oninput={e: Event ⇒
            name.value_=(e.target.asInstanceOf[Input].value.trim)
            nameEmpty.value_=(name.value.isEmpty)}/>
          </div>
          <div class="mt-2">
            {if (emailEmpty.bind) {
            <div class="bg-danger text-white">
              Email can't be invalid or empty
            </div>
          } else {
            <span style="display:none"></span>
          }}<label for="email">Email</label> <br/>
            <input name="email" type="text" autocomplete='email' value={UiContext.getStoredEmail}
                   oninput={e: Event ⇒
                     email.value_=(parseValidEmail(e.target.asInstanceOf[Input].value.trim).getOrElse(""))
                     emailEmpty.value_=(email.value.isEmpty)}/>
            <br/>
          </div>{new InputVarWithPassword(password, "Password").dom.bind}<div class="password-info">
          this is a high security system
          <br/>
          your password is not stored anywhere
          <br/>
          it is never sent over the internet
          <br/>
          if you forget it, it cannot be retrieved
        </div>{if (inProgress.bind) {
          <span class="active-info">Crunching cryptography... this is hard...</span>
        } else {
          <span>
            <input type="submit" class="btn btn-primary" value="Login"/>
            <div class="btn btn-secondary" onclick={onSignup _}>Sign-up</div>
          </span>
        }}

        </form>
      </div>
    } else {
      <span style="display:none"></span>
    }
  }

  private val emailRegex =
    """(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|"
      |(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*")@(?:(?:[a-z0-9]
      |(?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}
      |(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:
      |(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])""".r

  private def parseValidEmail(str: String): Option[String] = {
    emailRegex.findFirstIn(str)
  }
}
