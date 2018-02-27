package life.plenty.model.actions

import life.plenty.model.connection._
import life.plenty.model.octopi._
import life.plenty.model.octopi.definition.{Hub, Module}
import life.plenty.model.utils.GraphUtils
import rx.Ctx

class ActionSignup(override val hub: SignupQuestion) extends Module[SignupQuestion] {
  private implicit val ctx: Ctx.Owner = Ctx.Owner.safe()
  private lazy val contributing = GraphUtils.markedContributing(hub)

  def signup(who: User) = {

    val c = new Contribution()
    c.asNew(Body(""), Parent(hub))
  }

  def designup(who: User) = ???
}

class ActionAddConfirmedMarker(override val hub: Hub) extends Module[Hub] {
  private implicit val ctx = hub.ctx

  def confirm() = {
    hub.addConnection(Marker(MarkerEnum.CONFIRMED))
    println(s"added confirm marker ${hub.sc.all}")
    println(s"${hub.rx.cons}")
  }

  def deconfirm() = {
    GraphUtils.confirmedMarker(hub).now.foreach { m ⇒
      println(s"marker is active ${m.isActive.now}")
      m.inactivate()

      println(s"removed marker ${m.sc.all}")
      println(s"marker is active ${m.isActive.now}")
      println(s"${hub.rx.cons}")
    }

  }
}

class ActionAddContributor(override val hub: Contribution) extends Module[Contribution] {
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

class ActionAddMember(override val hub: WithMembers) extends Module[WithMembers] {

  def addMember(u: User) = {
    ???
  }

}

class ActionAddDescription(override val hub: Space) extends Module[Space] {
  private implicit val ctx = hub.ctx
  def add(body: String) = {
    val existing = hub.rx.get({ case c: Body ⇒ c })
    existing.foreach { cOpt ⇒
      existing.kill()
      cOpt foreach { c ⇒
        // fixme
//        withinOctopus.addConnection(Removed(c.id))
      }
    }
    hub.addConnection(Body(body))
  }
}