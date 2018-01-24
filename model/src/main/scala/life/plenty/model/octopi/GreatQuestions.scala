package life.plenty.model.octopi

trait GreatQuestion extends Space with WithParent[Space] {
  override def idGenerator: String = {
    println(this, "id gen is called", _parent, parent.getSafe, _title, title.getSafe)
    super.idGenerator + this.getClass.getSimpleName
  }
}

object GreatQuestions {

  val orderedConstructors: List[Space ⇒ GreatQuestion] =
    List(new Who(_), new Why(_), new What(_), new When(_), new Where(_), new How(_))

  class Why(override val _parent: Space) extends GreatQuestion {
    override lazy val _title: String = "Why"
  }

  object Why extends InstantiateByApply[Why] {
    def instantiate = new Why(null)
  }

  class When(override val _parent: Space) extends GreatQuestion {
    override lazy val _title: String = "When"
  }

  object When extends InstantiateByApply[When] {
    def instantiate = new When(null)
  }

  class Where(override val _parent: Space) extends GreatQuestion {
    override lazy val _title: String = "Where"
  }

  object Where extends InstantiateByApply[Where] {
    def instantiate = new Where(null)
  }

  class What(override val _parent: Space) extends GreatQuestion {
    override lazy val _title: String = "What"
  }

  object What extends InstantiateByApply[What] {
    def instantiate = new What(null)
  }

  class How(override val _parent: Space) extends GreatQuestion {
    override lazy val _title: String = "How"
  }

  object How extends InstantiateByApply[How] {
    def instantiate = new How(null)
  }

  class Who(override val _parent: Space) extends GreatQuestion {
    override lazy val _title: String = "Who"
  }

  object Who extends InstantiateByApply[Who] {
    def instantiate = new Who(null)
  }

}
