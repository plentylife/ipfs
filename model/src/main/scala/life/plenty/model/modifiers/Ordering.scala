package life.plenty.model.modifiers

import life.plenty.model
import life.plenty.model.console
import life.plenty.model.hub._
import life.plenty.model.hub.definition.Hub
import rx.{Ctx, Rx}

class AnswerVoteOrder(override val hub: Question) extends OctopusOrdering[Question] {

  model.console.trace(s"AnswerVoteOrder was instantiated in $hub")

  //  private implicit val ctx: Ctx.Owner = Ctx.Owner.safe()

  override def order(what: List[Hub]): List[Hub] = {
    var answers = List[Answer]()
    var others = List[Hub]()
    for (o ← what) {
      o match {
        case a: Answer ⇒ answers = a :: answers
        case o: Hub ⇒ others = o :: others
      }
    }

    answers = answers.sortBy(_.votes.now).reverse

    model.console.println(s"Sorting answers ${answers.map(a ⇒ a.getBody.now → a.votes.now)}")
    answers ::: others
  }

  override def applyRx(whatRx: Rx[List[Hub]])(implicit ctx: Ctx.Owner): Rx[List[Hub]] = whatRx.map {
    what ⇒
      val byVote: List[(Hub, Int)] = what.map {
        case a: Answer ⇒ a -> a.votes()
        case o: Hub ⇒ o -> 0
      }
      console.trace(s"rxSort answers ${byVote}")
      byVote.sortBy(_._2).reverse.map(_._1)
  }
}
