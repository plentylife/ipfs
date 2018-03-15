package life.plenty.ui.display

import com.thoughtworks.binding.Binding.BindingSeq
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.hub.{Answer, Question, SignupQuestion, User}
import life.plenty.model.hub.definition.Hub
import life.plenty.model.utils.GraphUtils; import life.plenty.model.utils.GraphExtractors
import life.plenty.model.utils.GraphUtils._
import life.plenty.ui.display.actions.SpaceActionsBar
import life.plenty.ui.display.meta.LayoutModule
import life.plenty.ui.display.utils.CardNavigation
import life.plenty.ui.model.DisplayModule
import life.plenty.ui.display.utils.Helpers._
import life.plenty.ui.model._
import org.scalajs.dom.{Event, Node}
import rx.Rx
import scalaz.std.list._

//{displayModules(siblingModules.withFilter(_.isInstanceOf[SpaceActionsBar]), "card-space-menu").bind}

object CardQuestionDisplayBase {
  @dom
  def html(hub: Question, body: List[Binding[Node]], onclick: (Event) ⇒ Unit): Binding[Node] = {
    implicit val ctx = hub.ctx
    val isConfirmed: BasicBindable[Boolean] = GraphExtractors.markedConfirmed(hub)
    val confirmedCss = if (isConfirmed().bind) " confirmed " else ""

    <div class={"card d-inline-flex flex-column question " + confirmedCss} id={hub.id}>
      <span class="d-flex header-block" onclick={onclick}>
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

      {if (body.nonEmpty) {
      <div class="card-body">
        {for (s <- body) yield s.bind}
      </div>
    } else DisplayModel.nospan.bind }
    </div>
  }
}

abstract class CardQuestionDisplayBase(override val hub: Question) extends LayoutModule[Question] with CardNavigation {
  override def doDisplay() = true

  private lazy val isConfirmed: BasicBindable[Boolean] = GraphExtractors.markedConfirmed(hub)

  @dom
  override protected def generateHtml(): Binding[Node] = {
    val cos: Seq[ModuleOverride] = this.cachedOverrides.bind
    val inlineQuestions =
      ComplexModuleOverride(this, {case m: InlineQuestionDisplay ⇒ m}, _.isInstanceOf[CardQuestionDisplayBase])
    implicit val os = inlineQuestions :: cos.toList ::: siblingOverrides

    CardQuestionDisplayBase.html(hub, body, navigateTo).bind
  }

  def body(implicit os: List[ModuleOverride]): List[Binding[Node]]
}

class CardQuestionDisplay(hub: Question) extends CardQuestionDisplayBase(hub) {
  override def body(implicit os: List[ModuleOverride]): List[Binding[Node]] = List(
    displayHubsF(children.withFilter(_.isInstanceOf[Answer]), "answers"),
    displayHubsF(children.withFilter(_.isInstanceOf[Question]), "inner-questions")
  )
}

class CardSignupQuestionDisplay(hub: SignupQuestion) extends CardQuestionDisplayBase(hub) {
  @dom
  private def users: Binding[Node] = {
    // fixme. contributing question should not display proposal creators
    val users: List[Rx[Option[User]]] = children.bind.collect{case a: Answer ⇒
      a.getCreator
    } toList;

    val dedupUsers = Rx {
      users.foldLeft(List[User]())((l: List[User], rxu) ⇒ {
        rxu() map {u ⇒
          if (!l.exists(_.id == u.id)) u :: l else l
        } getOrElse l
      })
    }

    val dub = new ListBindable(dedupUsers)

    <span>
      {for (u <- dub()) yield DisplayModel.display(u, overrides, this).bind}
    </span>
  }

  override def body(implicit os: List[ModuleOverride]): List[Binding[Node]] = List(
    users,
    displayHubsF(children.withFilter(_.isInstanceOf[Question]), "inner-questions")
  )
}