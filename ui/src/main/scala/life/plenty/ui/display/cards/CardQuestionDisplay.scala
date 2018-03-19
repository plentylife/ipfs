package life.plenty.ui.display.cards

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.hub._
import life.plenty.model.hub.definition.StateList
import life.plenty.model.utils.{DeprecatedGraphExtractors, GraphUtils}
import life.plenty.model.utils.GraphExtractors._
import life.plenty.ui.display.{FullUserBadge, InlineQuestionDisplay}
import life.plenty.ui.display.meta.LayoutModule
import life.plenty.ui.display.utils.{CardNavigation, DomOpStream, DomValStream}
import life.plenty.ui.display.utils.Helpers._
import life.plenty.ui.model._
import monix.reactive.Observable
import org.scalajs.dom.{Event, Node}
import rx.Rx
import scalaz.std.list._

//{displayModules(siblingModules.withFilter(_.isInstanceOf[SpaceActionsBar]), "card-space-menu").bind}

object CardQuestionDisplayBase {
  @dom
  def html(hub: Question, body: List[Binding[Node]], onclick: (Event) ⇒ Unit): Binding[Node] = {
    implicit val ctx = hub.ctx
    val isConfirmed = new DomValStream(isMarkedConfirmed(hub))
    val confirmedCss = if (isConfirmed.dom.bind) " confirmed " else ""

    <div class={"card d-inline-flex flex-column question " + confirmedCss} id={hub.id}>
      <span class="d-flex header-block" onclick={onclick}>
        <span class="d-flex title-block">
          <h5 class="card-title">{new DomValStream(hub.title).dom.bind}</h5>
          <div class="card-subtitle mb-2 text-muted">
            {new DomValStream(getBody(hub)).dom.bind}
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
    val users = new StateList(hub.getStream({case c: Contribution ⇒ c})).flatMap(c ⇒ c.creator)

    println("Users")
    hub.getStream({case c: Contribution ⇒ c}).dump("U:")
    users.stream.dump("UOP:")

    <span>
      {for (u <- new DomOpStream(users.get).v) yield FullUserBadge.html(u).bind}
    </span>
  }

  override def body(implicit os: List[ModuleOverride]): List[Binding[Node]] = List(
    users,
    displayHubsF(children.withFilter(_.isInstanceOf[Question]), "inner-questions")
  )
}