package life.plenty.ui.model
import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding.Var
import life.plenty.data.DbReader
import life.plenty.model.connection.{Creator, Email, Id, Name}
import life.plenty.model.octopi._
import life.plenty.model.octopi.definition.Hub
import life.plenty.model.security.SecureUser
import life.plenty.ui
import life.plenty.ui.display.{ErrorModal, LoadIndicator, Login}
import life.plenty.ui.supplemental.ErrorModals
import org.scalajs.dom.window
import rx.{Var ⇒ rxVar}

import scala.concurrent.ExecutionContext.Implicits.global

object UiContext {
  val userVar: Var[User] = Var(null)
  private val _pointer: Var[Option[Hub]] = Var(None)
  val pointerRx: rxVar[Option[Hub]] = rxVar(None)

  def startingSpace: Var[Option[Hub]] = _pointer
  def setStatingSpace(s: Hub) = {
    _pointer.value_=(Option(s))
    pointerRx.update(Option(s))
  }

  def getUser: User = userVar.value

  def getCreator = Creator(getUser)

  def getStoredEmail: String = Option(window.localStorage.getItem("useremail")).getOrElse("")

  def setUser(u: User) = {
    userVar.value_=(u)
    ui.console.trace(s"UiContext has set userVar set to ${userVar.value} ${userVar.value.id}")
  }

  def storeUser(email: String) = {
    window.localStorage.setItem("useremail", email)
  }

  def login(name: String, email: String, password: String) = {
    storeUser(email)
    println("making secure user object")
    val user = SecureUser(email, password)
    println("made secure user object")
    DbReader.exists(user.id) foreach {
      case true ⇒
        println("exists true")
        setUser(user)
      case false ⇒
        println("exists false")
        if (name == null || name.isEmpty) {
          ErrorModal.setContentAndOpen(ErrorModals.noSuchUserFound)
          Login.setFinished()
        } else createAndSetUser(name, email, user)
    }
  }

  private def createAndSetUser(name: String, email: String, user: SecureUser): Unit = {
    if (name != null && email != null && name.nonEmpty && email.nonEmpty) {
      println(s"createAndSetUser $name $email ${user.id}")
      user.asNew(Name(name), Email(email)) foreach {_ ⇒
        setUser(user)
      }
    } else {
      Login.setFinished()
      ui.console.error(s"UI could not create user from name `${Option(name)}` and email `${Option(email)}`")
    }
  }

  def devLogin = {
    println(s"dev login ${window.localStorage.getItem("p")}")
    Option(window.localStorage.getItem("p")) foreach { p ⇒
      Login.setInProgress()
      login(null, window.localStorage.getItem("useremail"), p)
    }
  }
}