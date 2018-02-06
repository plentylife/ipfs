package life.plenty.model.actions

import life.plenty.model.connection.{Body, Removed}
import life.plenty.model.octopi._

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
    ???
  }

}

class ActionAddDescription(override val withinOctopus: Space) extends Module[Space] {
  private implicit val ctx = withinOctopus.ctx
  def add(body: String) = {
    val existing = withinOctopus.rx.get({ case c: Body ⇒ c })
    existing.foreach { cOpt ⇒
      existing.kill()
      cOpt foreach { c ⇒
        withinOctopus.addConnection(Removed(c.id))
      }
    }
    withinOctopus.addConnection(Body(body))
  }
}