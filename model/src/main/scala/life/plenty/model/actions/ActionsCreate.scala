package life.plenty.model.actions

import life.plenty.model.connection.{Body, Parent, Title}
import life.plenty.model.octopi._
import life.plenty.model.octopi.definition.Module

class ActionCreateQuestion(override val withinOctopus: Space) extends Module[Space] {
  def create(title: String) = {
    val q = new BasicQuestion
    q.asNew(Parent(withinOctopus), Title(title))
  }
}

class ActionCreateAnswer(override val withinOctopus: Space) extends Module[Space] {
  def create(body: String, creator: User, isContribution: Boolean = false) = {
    val a = if (!isContribution) new Proposal else new Contribution
    a.asNew(Parent(withinOctopus), Body(body))
  }
}

/* fixme. this will probably be removed */
class InitializeMembersOctopus(override val withinOctopus: Space) extends ActionOnNew[Space] {
  override def onNew(): Unit = {
    //    withinOctopus.addConnection(Child(new Members(withinOctopus)))
  }
}