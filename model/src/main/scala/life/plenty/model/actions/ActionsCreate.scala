package life.plenty.model.actions

import life.plenty.model._
import life.plenty.model.connection.{Child, Contributor}

class ActionCreateQuestion(override val withinOctopus: Space) extends Module[Space] {
  def create(title: String) = {
    val q = new BasicQuestion(withinOctopus, title)
    //    println("created question ", q, "in", withinOctopus, withinOctopus.connections)
    withinOctopus.addConnection(Child(q))
  }
}

class ActionCreateAnswer(override val withinOctopus: Space) extends Module[Space] {
  def create(body: String, isContribution: Boolean = false) = {
    val a = if (!isContribution) new BasicAnswer(withinOctopus, body) else new Contribution(withinOctopus, body)
    withinOctopus.addConnection(Child(a))
  }
}

class ActionAddContributor(override val withinOctopus: Contribution) extends Module[Contribution] {
  def add(userId: String) = {
    val u = BasicUser(userId)
    val existing = withinOctopus.connections.collect({ case Contributor(u) ⇒ u })
    if (!existing.contains(u)) {
      withinOctopus.addConnection(Contributor(u))
      println("contributor added ", withinOctopus.connections)
    }
  }
}