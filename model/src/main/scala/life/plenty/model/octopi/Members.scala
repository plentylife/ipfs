package life.plenty.model.octopi

import life.plenty.model.connection.Member
import rx.Rx

class Members() extends WithParent[Space] {

  //  override def idGenerator: String = "membersof" + getParent.id

  // fixme. shouldn't that be in a module?
  //  def addMember(u: User) = {
  //    println("adding member", u.id, u)

    //    u.addConnection(Child(new Wallet(u, this, bi)))
    //    u.addConnection(Parent(this))
    //    new VoteAllowance(10, u, bi)
    //    this.addConnection(Member(u))
    //    println("adding member", u.id, u.connections)
  //  }

  //  def members: List[User] = this.connections.collect({ case Member(u) ⇒ u })

  lazy val members: Rx[List[User]] = rx.getAll({ case Member(u) ⇒ u })
}
