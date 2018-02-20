package life.plenty.model.actions

import life.plenty.model.connection._
import life.plenty.model.octopi._
import life.plenty.model.octopi.definition.{Hub, Module}
import life.plenty.model.utils.ConFinders

class ActionSignup(override val withinOctopus: SignupQuestion) extends Module[SignupQuestion] {
  def signup(who: User) = {
    val c = new Contribution()
    c.asNew(Body(""), Parent(withinOctopus))
  }

  def designup(who: User) = ???
}

class ActionAddConfirmedMarker(override val withinOctopus: Hub) extends Module[Hub] {
  private implicit val ctx = withinOctopus.ctx

  def confirm() = {
    withinOctopus.addConnection(Marker(MarkerEnum.CONFIRMED))
    println(s"added confirm marker ${withinOctopus.sc.all}")
    println(s"${withinOctopus.rx.cons}")
  }

  def deconfirm() = {
    ConFinders.confirmedMarker(withinOctopus).now.foreach {m ⇒
      println(s"marker is active ${m.isActive.now}")
      m.inactivate()

      println(s"removed marker ${m.sc.all}")
      println(s"marker is active ${m.isActive.now}")
      println(s"${withinOctopus.rx.cons}")
    }

  }
}

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
        // fixme
//        withinOctopus.addConnection(Removed(c.id))
      }
    }
    withinOctopus.addConnection(Body(body))
  }
}