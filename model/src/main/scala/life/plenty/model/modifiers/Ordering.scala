package life.plenty.model.modifiers

import life.plenty.model.octopi._

class AnswerVoteOrder(override val withinOctopus: Question) extends OctopusOrdering[Question] {

  override def order(what: List[Octopus]): List[Octopus] = {
    //    println("ordering answers")
    var answers = List[Answer]()
    var others = List[Octopus]()
    for (o ← what) {
      o match {
        case a: Answer ⇒ answers = a :: answers
        case o: Octopus ⇒ others = o :: others
      }
    }

    answers = answers.sortBy(_.votes.now).reverse
    answers ::: others
  }
}
