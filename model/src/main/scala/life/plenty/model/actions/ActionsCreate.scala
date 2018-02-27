package life.plenty.model.actions

import life.plenty.model.connection._
import life.plenty.model.octopi._
import life.plenty.model.octopi.definition.Module
import MarkerEnum.CONTRIBUTING_QUESTION

class ActionCreateQuestion(override val hub: Space) extends Module[Space] {
  def create(title: String, description: String, isSignup: Boolean = false, isContributing: Boolean = false) = {
    var params: List[DataHub[_]] = Parent(hub):: Title(title):: Body(description) :: Nil
    val q = if (isSignup) new SignupQuestion else new BasicQuestion
    if (isContributing) params = Marker(CONTRIBUTING_QUESTION) :: params
    println(s"create question $isSignup ${q.getClass} ${params}")
    q.asNew(params:_*)
  }
}

class ActionCreateAnswer(override val hub: Space) extends Module[Space] {
  def create(body: String, isContribution: Boolean = false) = {
    val a = if (!isContribution) new Proposal else new Contribution
    a.asNew(Parent(hub), Body(body))
  }
}

/* fixme. this will probably be removed */
class InitializeMembersOctopus(override val hub: Space) extends ActionOnNew[Space] {
  override def onNew(): Unit = {
    //    withinOctopus.addConnection(Child(new Members(withinOctopus)))
  }
}