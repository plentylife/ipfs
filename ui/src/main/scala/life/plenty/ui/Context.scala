package life.plenty.ui

import life.plenty.model.GraphUtils
import life.plenty.model.connection.Child
import life.plenty.model.octopi._

object Context {
  val userId = "anton"
  var startingSpace: Space = null

  def getUser: User = {
    //    println("getting current user")
    GraphUtils.findModuleUpParentTree(startingSpace, { case Child(m: Members) ⇒ m }).flatMap(m ⇒ {
      //      println("members", m, m.members)
      m.members.find(_.id == userId)
    }) getOrElse new BasicUser("anton-not-found")
  }

  //  def getUser: User = new BasicUser("anton")
}
