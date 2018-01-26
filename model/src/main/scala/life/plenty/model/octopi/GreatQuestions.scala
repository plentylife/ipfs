package life.plenty.model.octopi

import life.plenty.model.connection.Title

trait GreatQuestion extends Space with WithParent[Space] {
  override def idGenerator: String = {
    super.idGenerator + this.getClass.getSimpleName
  }
}

object GreatQuestions {

  class Why() extends GreatQuestion {
    addConnection(Title("Why"))
  }

  class When() extends GreatQuestion {
    addConnection(Title("When"))
  }

  class Where() extends GreatQuestion {
    addConnection(Title("Where"))
  }

  class What() extends GreatQuestion {
    addConnection(Title("What"))
  }

  class How() extends GreatQuestion {
    addConnection(Title("How"))
  }

  class Who() extends GreatQuestion {
    addConnection(Title("Who"))
  }

}
