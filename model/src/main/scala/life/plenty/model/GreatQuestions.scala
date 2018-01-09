package life.plenty.model

trait GreatQuestion extends Space with WithParent[Space]

object GreatQuestions {

  val orderedConstructors: List[Space ⇒ GreatQuestion] =
    List(new Why(_), new When(_), new Where(_))

  class Why(override val parent: Space) extends GreatQuestion {
    override val title: String = "Why"
  }

  class When(override val parent: Space) extends GreatQuestion {
    override val title: String = "When"
  }

  class Where(override val parent: Space) extends GreatQuestion {
    override val title: String = "Where"
  }

}
