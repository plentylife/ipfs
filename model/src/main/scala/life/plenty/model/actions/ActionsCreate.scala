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
  def create(body: String) = {
    val a = new BasicAnswer(withinOctopus, body)
    withinOctopus.addConnection(Child(a))
  }
}