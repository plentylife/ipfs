package life.plenty.ui.display.layouts

import com.thoughtworks.binding.Binding.{BindingSeq, Var ⇒ bVar}
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.connection.{Child, DataHub}
import life.plenty.model.hub._
import life.plenty.model.hub.definition.GraphOp
import life.plenty.model.hub.definition.GraphOp._
import life.plenty.model.utils.{DeprecatedGraphExtractors, GraphExtractors}
import life.plenty.ui.display.{FullUserBadge, TopSpaceLayout}
import life.plenty.ui.display.actions.SignupButton
import life.plenty.ui.display.utils.{DomOpStream, DomStream, DomValStream}
import life.plenty.ui.display.utils.Helpers._
import life.plenty.ui.model.{DisplayModel, ModuleOverride}
import monix.reactive.Observable
import org.scalajs.dom.raw.Node
import rx.{Obs, Rx}
import scalaz.std.list._
import scalaz.std.option._
import monix.execution.Scheduler.Implicits.global

class SignupQuestionSpaceLayout(override val hub: Space) extends TopSpaceLayout(hub) {
  private lazy val signup = new BindableModule(hub.getTopModule({case m: SignupButton ⇒ m}), this)
  private lazy val contributing = new DomValStream(GraphExtractors.isMarkedContributing(hub))

  contributing.stream foreach {c ⇒
    if (c) additionalCss.value_=("contributing-signup") else additionalCss.value_=("basic-signup")
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
  lazy val allProposals = hub.getStream({case Child(a: Proposal) ⇒ a})
  lazy val proposalsWithBody: Observable[GraphOp[(Proposal, Boolean)]] =
    new GraphOpsStream(allProposals).depMap(p ⇒ p.body.map(p → _.isEmpty))
  lazy val signers = proposalsWithBody.depMap({
    case (p, isEmpty) ⇒ if (isEmpty) p.creator else Observable.empty[User]
  })

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

  lazy val aB: ListBindable[Binding[Node]] =
    new ListBindable(Rx{List()})

  val ab = new DomOpStream[Proposal](proposalsWithBody.collectOps({case (p, false) ⇒ p}))
  val sb = new DomOpStream(signers)

  @dom
  private def basicSignupSection: Binding[Node] = if (!contributing.dom.bind) {
    println("Signers")
    for (c ← children.bind) println(s"CHILD $c")
    println(s"ALL CONS: ${hub.connections}")
    allProposals.dump("PROP:").subscribe()
    signers.dump("SIGN:").subscribe()

    <div class="basic-signup section">
      {signup.dom.bind}
      {for (u <- sb.v) yield FullUserBadge.html(u).bind}
    </div>

  } else {DisplayModel.nospan.bind}
}

