package life.plenty.model.octopi

import life.plenty.model.connection.{Connection, Title}

trait GreatQuestion extends Space with WithParent[Space] {
  protected val t: Title

  //  override def idGenerator: String = {
  //    super.idGenerator + this.getClass.getSimpleName
  //  }

  override def asNew(properties: Connection[_]*): Unit = {
    val ps = properties.:+(t)
    super.asNew(ps: _*)
  }
}

object GreatQuestions {

  class Why() extends GreatQuestion {
    protected lazy val t = Title("Why")
  }

  class When() extends GreatQuestion {
    protected lazy val t = Title("When")
  }

  class Where() extends GreatQuestion {
    protected lazy val t = Title("Where")
  }

  class What() extends GreatQuestion {
    protected lazy val t = Title("What")
  }

  class How() extends GreatQuestion {
    protected lazy val t = Title("How")
  }

  class Who() extends GreatQuestion {
    protected lazy val t = Title("Who")
  }

}
