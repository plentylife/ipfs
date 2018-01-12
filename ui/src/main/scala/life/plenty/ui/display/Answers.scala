package life.plenty.ui.display

import com.thoughtworks.binding.Binding.{Var, Vars}
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.connection.Contributor
import life.plenty.model.{Answer, User}
import life.plenty.ui.model.DisplayModel
import life.plenty.ui.model.DisplayModel.{DisplayModule, ModuleOverride}
import org.scalajs.dom.raw.Node

class AnswerDisplay(override val withinOctopus: Answer) extends DisplayModule[Answer] {
  protected val body = Var[String](withinOctopus.body)

  override def update(): Unit = body.value_=(withinOctopus.body)

  @dom
  override protected def generateHtml(overrides: List[DisplayModel.ModuleOverride]): Binding[Node] = {
    <div>
      answer:
      {body.bind}
    </div>
  }

}

class ContributionDisplay(override val withinOctopus: Answer) extends AnswerDisplay(withinOctopus) {
  private val _contributors = Vars(findContributors: _*)
  override def update(): Unit = {
    super.update()
    _contributors.value.clear()
    _contributors.value.insertAll(0, findContributors)
  }
  private def findContributors = withinOctopus.connections.collect {
    case Contributor(c: User) ⇒ c
  }

  override def overrides: List[DisplayModel.ModuleOverride] =
    ModuleOverride(this, new NoDisplay(withinOctopus),
      m ⇒ m.isInstanceOf[AnswerDisplay] && !m.isInstanceOf[ContributionDisplay]) :: super.overrides
  @dom
  override def generateHtml(overrides: List[DisplayModel.ModuleOverride]): Binding[Node] = {
    <div>
      contribution:
      {body.bind}<br/>
      ---
      <br/>
      contributors:
      {for (c <- _contributors) yield displayContributor(c).bind}
    </div>
  }

  @dom
  private def displayContributor(user: User): Binding[Node] = <div>
    {user.id}
  </div>
}