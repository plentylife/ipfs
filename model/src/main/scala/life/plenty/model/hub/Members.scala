package life.plenty.model.hub

import life.plenty.model
import life.plenty.model.connection.{Member, Parent}
import rx.Rx

class Members extends WithParent[Space] {
  lazy val getMembers: Rx[List[User]] = rx.getAll({ case Member(u) ⇒ u })

  // fixme. needs to wait for load
  // at the same time, there shouldn't be any hard done, because the connections aren't changed, and they have
  // the same ids, etc.
  def addMember(u: User) = {

    model.console.trace("Trying to Adding a new member")
    val existing: Rx[Boolean] = getMembers.map { ms ⇒
      ms.exists(_.id == u.id)
    }

    existing.foreach { ex ⇒
      if (!ex) {
        model.console.println(s"Adding a new member ${u} in ${this}")
        u.addConnection(Parent(this))
        addConnection(Member(u))
        existing.kill()
      }
    }

  }
}