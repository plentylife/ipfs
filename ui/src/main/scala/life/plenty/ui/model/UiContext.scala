package life.plenty.ui.model
import com.thoughtworks.binding.Binding.Var
import life.plenty.data.OctopusReader
import life.plenty.model.connection.{Creator, Id, Name}
import life.plenty.model.octopi._
import org.scalajs.dom.window

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

object UiContext {
  val userVar: Var[User] = Var(null)
  val startingSpace: Var[Option[Space]] = Var(None)

  def storeUser(name: String, email: String) = {
    window.localStorage.setItem("username", name)
    window.localStorage.setItem("useremail", email)
    Try(loadUser()).recover({
      case e: Throwable ⇒ createAndSetUser(name, email)
    })
  }

  def setUser(u: BasicUser) = {
    userVar.value_=(u)
  }

  def login(name: String, email: String) = {
    storeUser(name, email)
    //    Main.showUi()
  }

  def loadUser() = {
    val name = window.localStorage.getItem("username")
    val email = window.localStorage.getItem("useremail")
    //    createAndSetUser(name, email)
    if (name != null && email != null) {
      OctopusReader.read(generateUserId(name, email)).foreach {
        case Some(u) ⇒ setUser(u.asInstanceOf[BasicUser])
        case None ⇒ println("UiContext was unable to load user given the stored credentials")
      }
    }
  }

  private def generateUserId(n: String, e: String) = e

  private def createAndSetUser(name: String, email: String) = {
    if (name != null && email != null && name.nonEmpty && email.nonEmpty) {
      println(s"createAndSetUser $name $email")
      val u = new BasicUser
      u.asNew(Id(generateUserId(name, email)), Name(name))
      setUser(u)
    } else {
      println(s"UI could not create user from name `${Option(name)}` and email `${Option(email)}`")
    }
  }

  def getUser: User = userVar.value

  def getCreator = Creator(getUser)

  def initialize() = {
    loadUser()
  }
}


//    //    println("getting current user")
//    GraphUtils.findModuleUpParentTree(startingSpace, { case Child(m: Members) ⇒ m }).flatMap(m ⇒ {
//      //      println("members", m, m.members)
//      m.members.find(_.id == userId)
//    }) getOrElse {
//      val u = new BasicUser()
//      u.asNew(Id("anton-not-found"))
//      u
//    }
//  }