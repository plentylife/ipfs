package life.plenty.model.actions

import life.plenty.model._
import life.plenty.model.connection.Child

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

class InitializeMembersOctopus(override val withinOctopus: Space) extends ActionOnInitialize[Space] {
  override def onInitialize(): Unit = {
    withinOctopus.addConnection(Child(new Members(withinOctopus)))
  }
}