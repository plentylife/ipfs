package life.plenty.ui.model
import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding.Var
import life.plenty.data.OctopusReader
import life.plenty.model.connection.{Creator, Email, Id, Name}
import life.plenty.model.octopi._
import life.plenty.model.security.SecureUser
import life.plenty.ui
import life.plenty.ui.display.{ErrorModal, ErrorModals}
import org.scalajs.dom.window
import rx.{Var ⇒ rxVar}

import scala.concurrent.ExecutionContext.Implicits.global

object UiContext {
  val userVar: Var[User] = Var(null)
  private val _startingSpace: Var[Option[Space]] = Var(None)
  val startingSpaceRx: rxVar[Option[Space]] = rxVar(None)

  def startingSpace: Var[Option[Space]] = _startingSpace
  def setStatingSpace(s: Option[Space]) = {
    _startingSpace.value_=(s)
    startingSpaceRx.update(s)
  }

  def getUser: User = userVar.value

  def getCreator = Creator(getUser)

  def getStoredEmail: String = Option(window.localStorage.getItem("useremail")).getOrElse("")

  def setUser(u: User) = {
    userVar.value_=(u)
    ui.console.trace(s"UiContext has set userVar set to ${userVar.value}")
  }

  def storeUser(email: String) = {
    window.localStorage.setItem("useremail", email)
  }

  def login(name: String, email: String, password: String) = {
    storeUser(email)
    val user = SecureUser(email, password)
    OctopusReader.exists(user.id) foreach {
      case true ⇒ setUser(user)
      case false ⇒
        if (name == null || name.isEmpty) {
          ErrorModal.setContentAndOpen(ErrorModals.noSuchUserFound)
        } else createAndSetUser(name, email, user)
    }
  }

  private def createAndSetUser(name: String, email: String, user: SecureUser): Unit = {
    if (name != null && email != null && name.nonEmpty && email.nonEmpty) {
      println(s"createAndSetUser $name $email")
      user.asNew(Id(user.id), Name(name), Email(email)) foreach {_ ⇒
        setUser(user)
      }
    } else {
      ui.console.error(s"UI could not create user from name `${Option(name)}` and email `${Option(email)}`")
    }
  }
}