package life.plenty.model.modifiers

import life.plenty.model.console
import life.plenty.model.octopi._
import rx.{Ctx, Rx}

class AnswerVoteOrder(override val withinOctopus: Question) extends OctopusOrdering[Question] {

  println(s"AnswerVoteOrder was instantiated in $withinOctopus")

  //  private implicit val ctx: Ctx.Owner = Ctx.Owner.safe()

  override def order(what: List[Octopus]): List[Octopus] = {
    println("ordering answers")
    var answers = List[Answer]()
    var others = List[Octopus]()
    for (o ← what) {
      o match {
        case a: Answer ⇒ answers = a :: answers
        case o: Octopus ⇒ others = o :: others
      }
    }

    answers = answers.sortBy(_.votes.now).reverse

    println(s"Sorting answers ${answers.map(a ⇒ a.getBody.now → a.votes.now)}")
    answers ::: others
  }

  override def applyRx(whatRx: Rx[List[Octopus]])(implicit ctx: Ctx.Owner): Rx[List[Octopus]] = whatRx.map {
    what ⇒
      val byVote: List[(Octopus, Int)] = what.map {
        case a: Answer ⇒ a -> a.votes()
        case o: Octopus ⇒ o -> 0
      }
      console.println(s"rxSort answers ${byVote}")
      byVote.sortBy(_._2).reverse.map(_._1)
  }
}
