package life.plenty.model.actions

import life.plenty.model
import life.plenty.model.connection._
import life.plenty.model.octopi._
import rx.{Ctx, Rx}

class ActionAddContributor(override val withinOctopus: Contribution) extends Module[Contribution] {
  def add(userId: String) = {
    ???
    //    val u = new BasicUser(userId)
    //    val existing = withinOctopus.connections.collect({ case Contributor(u) ⇒ u })
    //    if (!existing.contains(u)) {
    //      withinOctopus.addConnection(Contributor(u))
    //      println("contributor added ", withinOctopus.connections)
    //    }
  }
}

class ActionAddMember(override val withinOctopus: WithMembers) extends Module[WithMembers] {

  def addMember(u: User) = {
      model.console.println("Trying to Adding a new member")
    Members.proposeTask(task(u))
    Right()
  }

  //todo. the context thing is off it seems
  private def task(u: User)(members: Members, ctx: Ctx.Owner): Unit = {
    implicit val c = ctx

    model.console.println("Task executing")

    members.getParent.foreach { p ⇒
      model.console.println(s"Task condition ${p}")
      if (p.forall(_.id != withinOctopus.id)) return
      model.console.println(s"Task condition passed ${members.getMembers.now}")

      val existing: Rx[Boolean] = members.getMembers.map { ms ⇒
        ms.exists(_.id == u.id)
      }
      existing.foreach { ex ⇒
        if (!ex) {
          model.console.println("Adding a new member")
          val w = new Wallet
          w.asNew(Parent(u), Parent(members))
          u.addConnection(Child(w))
          u.addConnection(Parent(members))
          members.addConnection(Member(u))
        }
      }

    }
  }

}