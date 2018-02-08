package life.plenty.model.actions

import life.plenty.model.connection.{Body, Marker, MarkerEnum, Removed}
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

class ActionAddConfirmedMarker(override val withinOctopus: Octopus) extends Module[Octopus] {
  private implicit val ctx = withinOctopus.ctx

  def confirm() = {
    withinOctopus.addConnection(Marker(MarkerEnum.CONFIRMED))
    println(s"added confirm marker ${withinOctopus.sc.all}")
    println(s"${withinOctopus.rx.cons}")
  }

  def deconfirm() = {
    // try just adding the same remove!
    withinOctopus.removeConnection(Marker(MarkerEnum.CONFIRMED))
    println(s"removed marker ${withinOctopus.sc.all}")
    println(s"${withinOctopus.rx.cons}")
    //    val obs = withinOctopus.rx.get({ case c@Marker(m) if m == MarkerEnum.CONFIRMED ⇒ c })
    //    obs.foreach(_ foreach { m ⇒
    //      println(s"Found confirm marker. Deconfirming ${m}")
    //      obs.kill()
    //      withinOctopus.removeConnection(m)
    //      println(s"removed marker ${withinOctopus.connections}")
    //    })
  }
}