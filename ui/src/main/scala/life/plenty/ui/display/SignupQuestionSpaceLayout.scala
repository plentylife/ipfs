package life.plenty.ui.display

import com.thoughtworks.binding.Binding.{BindingSeq, Var ⇒ bVar, Vars}
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.connection.Child
import life.plenty.model.hub._
import life.plenty.model.hub.definition.Hub
import life.plenty.model.utils.GraphUtils; import life.plenty.model.utils.DeprecatedGraphExtractors
import life.plenty.ui.display.actions.{SignupButton, SpaceActionsBar}
import life.plenty.ui.display.meta.LayoutModule
import life.plenty.ui.display.utils.Helpers._
import life.plenty.ui.model.{DisplayModel, ModuleOverride, UiContext}
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.Node
import rx.{Obs, Rx}
import scalaz.std.list._
import scalaz.std.option._

class SignupQuestionSpaceLayout(override val hub: Space) extends TopSpaceLayout(hub) {
  private lazy val signup = new BindableModule(hub.getTopModule({case m: SignupButton ⇒ m}), this)
  private lazy val contributing = DeprecatedGraphExtractors.markedContributing(hub)
  private lazy val contributingB = bVar(false)

  private var obs: Obs = null
  override def update(): Unit = {
    if (obs == null) obs = contributing foreach {c ⇒
        if (c) additionalCss.value_=("contributing-signup") else additionalCss.value_=("basic-signup")
        contributingB.value_=(c)
      }

    super.update()
  }

  @dom
  private def ifEmpty: Binding[Node] = <span class="mt-2">:( no one has signed up yet to help</span>

  override protected def sections(implicit overrides: List[ModuleOverride]): List[Binding[Node]] = List(
    {displayHubs(getContributions(children), "contributions section", signup.dom, ifEmpty)},
    basicSignupSection,
    displayHubsF(getMembers(children), "administrative section"),
    displayHubsF(getQuestions(children), "questions section"),
    displayHubNodes(aB(), "answers section"),
    displayHubsF(getSubSpaces(children), "sub-spaces section")
  )

//  lazy val allProposals = children.value.toList.collect({case a: Proposal ⇒ a})
//  lazy val allProposals = rxChildren.map{_.collect({case a: Proposal ⇒ a})}
//
//  lazy val signers: Rx[List[User]] = Rx {
//    val answers = allProposals().filter(_.getBody().exists(_.isEmpty))
//    answers map {_.getCreator()} flatten;
//  }
//  lazy val sB: ListBindable[User] = new ListBindable(signers)
//
//  lazy val nonEmptyAnswers = Rx {
//    allProposals().filter(_.getBody().exists(_.nonEmpty))
//  }
//  lazy val aB: ListBindable[Binding[Node]] =
//    new ListBindable(nonEmptyAnswers map {_ map { a => DisplayModel.display(a, overrides, Option(this))}})

    lazy val sB: ListBindable[User] = new ListBindable(Rx{List()})
  lazy val aB: ListBindable[Binding[Node]] =
    new ListBindable(Rx{List()})


  @dom
  private def basicSignupSection: Binding[Node] = if (!contributingB.bind) {
    val l: BindingSeq[User] = sB()
    <div class="basic-signup section">
      {signup.dom.bind}
      {for (a <- l) yield DisplayModel.display(a, Nil, Option(this)).bind}
    </div>

  } else {DisplayModel.nospan.bind}
}

