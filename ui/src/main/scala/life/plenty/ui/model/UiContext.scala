package life.plenty.ui.model

import com.thoughtworks.binding.Binding.Var
import life.plenty.model.connection.{Creator, Id, Name}
import life.plenty.model.octopi._
import life.plenty.ui.Main
import org.scalajs.dom.window

object UiContext {
  private var user: User = null
  val startingSpace: Var[Option[Space]] = Var(None)

  def setUser(name: String, email: String) = {
    window.localStorage.setItem("username", name)
    window.localStorage.setItem("useremail", email)
    createAndSetUser(name, email)
  }

  def login(name: String, email: String) = {
    setUser(name, email)
    Main.showUi()
  }

  def loadUser() = {
    val name = window.localStorage.getItem("username")
    val email = window.localStorage.getItem("useremail")
    createAndSetUser(name, email)
  }

  private def createAndSetUser(name: String, email: String) = {
    if (name != null && email != null && name.nonEmpty && email.nonEmpty) {
      println(s"createAndSetUser $name $email")
      val u = new BasicUser
      u.asNew(Id(email + name), Name(name))
      user = u
    } else {
      println(s"UI could not create user from name `${Option(name)}` and email `${Option(email)}`")
    }
  }

  def getUser: User = user

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