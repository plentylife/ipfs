package life.plenty.ui.display

import com.thoughtworks.binding.Binding.BindingSeq
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.definition.Hub
import life.plenty.model.octopi._
import life.plenty.model.utils.GraphUtils
import life.plenty.model.utils.GraphUtils._
import life.plenty.ui.display.actions.SpaceActionsBar
import life.plenty.ui.display.meta.LayoutModule
import life.plenty.ui.display.utils.CardNavigation
import life.plenty.ui.model.DisplayModel.DisplayModule
import life.plenty.ui.display.utils.Helpers._
import life.plenty.ui.model.{ComplexModuleOverride, ModuleOverride, Router, UiContext}
import org.scalajs.dom.Node
import rx.Rx
import scalaz.std.list._

//{displayModules(siblingModules.withFilter(_.isInstanceOf[SpaceActionsBar]), "card-space-menu").bind}

abstract class CardQuestionDisplayBase(override val hub: Question) extends LayoutModule[Question] with CardNavigation {
  override def doDisplay() = true

  private lazy val isConfirmed: BasicBindable[Boolean] = GraphUtils.markedConfirmed(hub)

  @dom
  override protected def generateHtml(): Binding[Node] = {
    val cos: Seq[ModuleOverride] = this.cachedOverrides.bind
    val inlineQuestions =
      ComplexModuleOverride(this, {case m: InlineQuestionDisplay ⇒ m}, _.isInstanceOf[CardQuestionDisplayBase])
    implicit val os = inlineQuestions :: cos.toList ::: siblingOverrides
    var confirmedCss = if (isConfirmed().bind) " confirmed " else ""


    <div class={"card d-inline-flex flex-column question " + confirmedCss}
         id={hub.id}>
      <span class="d-flex header-block" onclick={navigateTo _}>
        <span class="d-flex title-block">
          <h5 class="card-title">{hub.getTitle.dom.bind}</h5>
          <div class="card-subtitle mb-2 text-muted">
            {getBody(hub).dom.bind}
          </div>
        </span>
        <span class="card-controls">
          <div class="btn btn-primary btn-sm">open</div>
        </span>
      </span>

      <div class="card-body">
          {for (s <- body) yield s.bind}
        </div>
    </div>
  }

  def body(implicit os: List[ModuleOverride]): List[Binding[Node]]
}

class CardQuestionDisplay(hub: Question) extends CardQuestionDisplayBase(hub) {
  override def body(implicit os: List[ModuleOverride]): List[Binding[Node]] = List(
    displayHubs(children.withFilter(_.isInstanceOf[Answer]), "answers"),
    displayHubs(children.withFilter(_.isInstanceOf[Question]), "inner-questions")
  )
}

class CardSignupQuestionDisplay(hub: SignupQuestion) extends CardQuestionDisplayBase(hub) {
  @dom
  private def users: Binding[Node] = {
    val users: List[Rx[Option[User]]] = children.bind.collect{case a: Answer ⇒
      a.getCreator
    } toList;

    <span>
      {for (u <- users) yield new OptBindableHub(u, this).dom.bind}
    </span>
  }

  override def body(implicit os: List[ModuleOverride]): List[Binding[Node]] = List(
    users,
    displayHubs(children.withFilter(_.isInstanceOf[Question]), "inner-questions")
  )
}