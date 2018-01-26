package life.plenty.model.octopi

import life.plenty.model.connection.{AtInstantiation, Title}

trait GreatQuestion extends Space with WithParent[Space] {
  override def idGenerator: String = {
    super.idGenerator + this.getClass.getSimpleName
  }
}

object GreatQuestions {

  class Why() extends GreatQuestion {
    private lazy val t = Title("Why")
    t.tmpMarker = AtInstantiation
    addConnection(t)
  }

  class When() extends GreatQuestion {
    private lazy val t = Title("When")
    t.tmpMarker = AtInstantiation
    addConnection(t)
  }

  class Where() extends GreatQuestion {
    private lazy val t = Title("Where")
    t.tmpMarker = AtInstantiation
    addConnection(t)
  }

  class What() extends GreatQuestion {
    private lazy val t = Title("What")
    t.tmpMarker = AtInstantiation
    addConnection(t)
  }

  class How() extends GreatQuestion {
    private lazy val t = Title("How")
    t.tmpMarker = AtInstantiation
    addConnection(t)
  }

  class Who() extends GreatQuestion {
    private lazy val t = Title("Who")
    t.tmpMarker = AtInstantiation
    addConnection(t)
  }

}
