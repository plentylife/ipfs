package life.plenty.ui

import life.plenty.model.octopi.{BasicUser, User}

object Context {
  def getUser: User = new BasicUser("anton")
}
