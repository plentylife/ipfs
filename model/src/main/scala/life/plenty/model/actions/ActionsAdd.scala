package life.plenty.model.actions

import life.plenty.model
import life.plenty.model.GraphUtils
import life.plenty.model.connection._
import life.plenty.model.octopi._
import rx.Ctx

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
  println("ActionAddMember")

  private lazy val membersOctopus = {
    findMembersOctopus()
  }

  def addMember(u: User) = membersOctopus match {
    case Some(mem) ⇒
      model.console.println("Trying to Adding a new member")
      // should get gc'ed
      implicit val ctx = Ctx.Owner.Unsafe
      val existing = mem.rx.get({ case Member(m) if m.id == u.id ⇒ m })

      if (existing.now.isEmpty) {
        model.console.println("Adding a new member")
        val w = new Wallet
        w.asNew(Parent(u), Parent(mem))
        u.addConnection(Child(w))
        u.addConnection(Parent(mem))
        mem.addConnection(Member(u))
      }

      Right()
    case _ ⇒
      model.console.error("Found no Members octopus")
      Left(new Exception("no Members octopus"))
  }

  private def findMembersOctopus(starting: Octopus = withinOctopus): Option[Members] = {
    model.console.println(s"Looking for Members in ${starting} ${starting.connections}")
    GraphUtils.findModuleUpParentTree(starting, { case Child(m: Members) ⇒ m })
  }
}