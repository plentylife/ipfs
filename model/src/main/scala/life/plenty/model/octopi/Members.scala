package life.plenty.model.octopi

import life.plenty.model.connection.{Child, Member}

class Members(override val parent: Space) extends WithParent[Space] {
  // fixme. shouldn't that be in a module?
  def addMember(u: User) = {
    println("adding member")

    u.addConnection(Child(new Wallet(u, this)))
    this.addConnection(Member(u))
  }

  def members: List[User] = this.connections.collect({ case Member(u) ⇒ u })
}
