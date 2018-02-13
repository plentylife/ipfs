package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.ui.model.UiContext
import org.scalajs.dom.html.Input
import org.scalajs.dom.{Event, Node}

object Login {
  //  private val open = Var(isOpen)

  private val name = Var("")
  private val email = Var("")

  private val emailEmpty = Var(false)
  private val nameEmpty = Var(false)

  @dom
  private def isOpen: Binding[Boolean] = {
    //    UiContext.getUser == null
    UiContext.userVar.bind == null
  }

  private def onSubmit(e: Event) = {
    if (name.value.nonEmpty && email.value.nonEmpty) {
      UiContext.login(name.value, email.value)
      //              open.value_=(isOpen)
    }
  }

  private val emailRegex =
    """(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])""".r
  private def parseValidEmail(str: String): Option[String] = {
    emailRegex.findFirstIn(str)
  }

  @dom
  def display(): Binding[Node] = {
    if (isOpen.bind) {
      <div class="login-outer-box d-flex flex-column justify-content-center align-items-center">
        <div class="d-flex logo">
          <img src="images/plenty_logo-400.png"/>
        </div>
        <form class="login-box d-inline-flex flex-column" onsubmit={e: Event ⇒
          e.preventDefault()
          onSubmit(e)}>
          <div>
            {if (nameEmpty.bind) {
            <div class="bg-danger text-white">
              Name can't be empty
            </div>
          } else {
            <span style="display:none"></span>
          }}<label for="name">Your name</label> <br/>
            <input name="name" type="text" oninput={e: Event ⇒
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
            <input name="email" type="text" oninput={e: Event ⇒
            email.value_=(parseValidEmail(e.target.asInstanceOf[Input].value.trim).getOrElse(""))
            emailEmpty.value_=(email.value.isEmpty)}/>
            <br/>
          </div>
          <input type="submit" class="btn btn-primary mt-2" value="Login"/>
        </form>
      </div>
    } else {
      <span style="display:none"></span>
    }
  }
}
