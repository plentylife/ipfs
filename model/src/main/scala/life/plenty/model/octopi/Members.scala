package life.plenty.model.octopi

import life.plenty.model.connection.{Child, Member, Parent}

class Members(override val _parent: Space, override val _basicInfo: BasicInfo) extends WithParent[Space] {

  override def idGenerator: String = "membersof" + parent()

  // fixme. shouldn't that be in a module?
  def addMember(u: User) = {
    println("adding member", u.id, u)

    val bi = new BasicInfo {
      // fixme?
      override lazy val creator: User = u
      override lazy val creationTime: Long = _this.creationTime.map(_.getTime).getOrElse(_basicInfo.creationTime)
    }

    u.addConnection(Child(new Wallet(u, this, bi)))
    u.addConnection(Parent(this))
    new VoteAllowance(10, u, bi)
    this.addConnection(Member(u))
    println("adding member", u.id, u.connections)
  }

  def members: List[User] = this.connections.collect({ case Member(u) ⇒ u })
}
