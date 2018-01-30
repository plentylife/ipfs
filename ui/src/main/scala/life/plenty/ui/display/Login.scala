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

  @dom
  def display(): Binding[Node] = {
    if (isOpen.bind) {
      <div class="login-outer-box d-flex justify-content-center align-items-center">
        <div class="login-box d-inline-flex flex-column">
          <div>
            {if (nameEmpty.bind) {
            <div class="bg-danger text-white">
              Name can't be empty
            </div>
          } else {
            <span style="display:none"></span>
          }}<label for="name">Your name</label> <br/>
            <input name="name" type="text" onchange={e: Event ⇒
            name.value_=(e.target.asInstanceOf[Input].value.trim)
            nameEmpty.value_=(name.value.isEmpty)}/>
          </div>
          <div class="mt-2">
            {if (emailEmpty.bind) {
            <div class="bg-danger text-white">
              Email can't be empty
            </div>
          } else {
            <span style="display:none"></span>
          }}<label for="email">Email</label> <br/>
            <input name="email" type="text" onchange={e: Event ⇒
            email.value_=(e.target.asInstanceOf[Input].value.trim)
            emailEmpty.value_=(email.value.isEmpty)}/>
            <br/>
          </div>
          <button type="button" class="btn btn-primary mt-2" onclick={e: Event ⇒
            if (name.value.nonEmpty && email.value.nonEmpty) {
              UiContext.login(name.value, email.value)
              //              open.value_=(isOpen)
            }}>Login</button>
        </div>
      </div>
    } else {
      <span style="display:none"></span>
    }
  }
}
