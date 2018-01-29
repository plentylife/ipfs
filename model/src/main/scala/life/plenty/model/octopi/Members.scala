package life.plenty.model.octopi

import life.plenty.model
import life.plenty.model.connection.{Child, Member, Parent}
import rx.Rx

class Members extends WithParent[Space] {
  lazy val getMembers: Rx[List[User]] = rx.getAll({ case Member(u) ⇒ u })

  // fixme. needs to wait for load
  def addMember(u: User) = {

    model.console.trace("Trying to Adding a new member")
    val existing: Rx[Boolean] = getMembers.map { ms ⇒
      ms.exists(_.id == u.id)
    }

    existing.foreach { ex ⇒
      if (!ex) {
        model.console.println(s"Adding a new member ${u} in ${this}")
        val w = new Wallet
        w.asNew(Parent(u), Parent(this))
        u.addConnection(Child(w))
        u.addConnection(Parent(this))
        addConnection(Member(u))
      }
    }

  }
}