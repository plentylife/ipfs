package life.plenty.model.octopi

trait GreatQuestion extends Space with WithParent[Space]

object GreatQuestions {

  val orderedConstructors: List[Space ⇒ GreatQuestion] =
    List(new Why(_), new What(_), new When(_), new Where(_), new How(_))

  class Why(override val parent: Space) extends GreatQuestion {
    override val title: String = "Why"
  }

  class When(override val parent: Space) extends GreatQuestion {
    override val title: String = "When"
  }

  class Where(override val parent: Space) extends GreatQuestion {
    override val title: String = "Where"
  }

  class What(override val parent: Space) extends GreatQuestion {
    override val title: String = "What"
  }

  class How(override val parent: Space) extends GreatQuestion {
    override val title: String = "How"
  }

}
