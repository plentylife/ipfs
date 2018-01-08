package life.plenty.model.actions

import life.plenty.model._

class ActionCreateQuestion(override val withinOctopus: Question) extends Module[Question] {
  def create(title: String) = {
    val q = new BasicQuestion(withinOctopus, title)
    withinOctopus.addConnection(Child(q))
  }
}
