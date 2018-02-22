package life.plenty.ui.model
import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding.Var
import life.plenty.data.OctopusReader
import life.plenty.model.connection.{Creator, Id, Name}
import life.plenty.model.octopi._
import life.plenty.ui
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

  def storeUser(name: String, email: String) = {
    window.localStorage.setItem("username", name)
    window.localStorage.setItem("useremail", email)
    loadUser()
  }

  def setUser(u: BasicUser) = {
    userVar.value_=(u)
    ui.console.trace(s"UiContext setUser has userVar set to ${userVar.value}")
  }

  def login(name: String, email: String, password: String) = {
    storeUser(name, email)
    //    Main.showUi()
  }

  def loadUser() = {
    val name = window.localStorage.getItem("username")
    val email = window.localStorage.getItem("useremail")
    //    createAndSetUser(name, email)
    if (name != null && email != null) {
      ui.console.trace(s"UiContext loadUser trying with ${name} ${email}")
      val uFuture = OctopusReader.read(generateUserId(name, email))
      // name and email present but not in the database
      uFuture.recover({
        case e: Throwable ⇒ ui.console.trace("loadUser in UiContext was unable to load the user from the database");
          createAndSetUser(name, email)
      })
      // present in the database
      uFuture.foreach {
        case Some(u) ⇒
          ui.console.trace(s"loadUser in UiContext loaded user ${u} ${u.id} from the database")
          setUser(u.asInstanceOf[BasicUser])
        case None ⇒ ui.console.error("UiContext was unable to load user from database given the stored credentials")
      }
    }
  }

  private def generateUserId(n: String, e: String) = e

  private def createAndSetUser(name: String, email: String): Unit = {
    if (name != null && email != null && name.nonEmpty && email.nonEmpty) {
      println(s"createAndSetUser $name $email")
      val u = new BasicUser
      u.asNew(Id(generateUserId(name, email)), Name(name)) foreach {_ ⇒
        setUser(u)
      }
    } else {
      ui.console.error(s"UI could not create user from name `${Option(name)}` and email `${Option(email)}`")
    }
  }

  def getUser: User = userVar.value

  def getCreator = Creator(getUser)

  def initialize() = {
    ui.console.trace("Initialize in UiContext")
    loadUser()
  }
}