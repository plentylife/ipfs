package life.plenty.model

import life.plenty.model.connection.Member

class Members(override val parent: Space) extends WithParent[Space] {
  def addMember(u: User) = {
    println("adding member")
    this.addConnection(Member(u))
  }

  def members: List[User] = this.connections.collect({ case Member(u) ⇒ u })
}
