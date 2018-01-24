package life.plenty.model.octopi

import life.plenty.model.connection.{Child, Member, Parent}

class Members(override val _parent: Space) extends WithParent[Space] {

  override def idGenerator: String = "membersof" + parent()

  // fixme. shouldn't that be in a module?
  def addMember(u: User) = {
    println("adding member", u.id, u)

    u.addConnection(Child(new Wallet(u, this)))
    u.addConnection(Parent(this))
    new VoteAllowance(10, u)
    this.addConnection(Member(u))
    println("adding member", u.id, u.connections)
  }

  def members: List[User] = this.connections.collect({ case Member(u) ⇒ u })
}
