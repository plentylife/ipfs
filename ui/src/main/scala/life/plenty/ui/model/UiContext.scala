package life.plenty.ui.model

import life.plenty.model.connection.{Creator, Id}
import life.plenty.model.octopi._

object UiContext {
  val userId = "anton"
  var startingSpace: Space = null

  def getUser: User = {
    val u = new BasicUser()
    u.asNew(Id(userId))
    u
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

  def getCreator = Creator(getUser)

  //  def getUser: User = new BasicUser("anton")
}