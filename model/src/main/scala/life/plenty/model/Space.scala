package life.plenty.model

import life.plenty.model.MarkerEnum.FILL_GREAT_QUESTIONS

trait Space extends Octopus {
  val title: String

  override protected def preConstructor(): Unit = {
    super.preConstructor()
    println("Space constructor")
    addConnection(Title(title))
    addConnection(Marker(FILL_GREAT_QUESTIONS))
  }

}

class AddGreatQuestions(override val withinOctopus: Space) extends ActionOnInitialize {
  override def onInitialize(): Unit = {
    println("on action initialize")
    println(withinOctopus.connections)
    withinOctopus.getTopConnection({ case m@Marker(FILL_GREAT_QUESTIONS) ⇒ m }).foreach(_ ⇒ fill())
  }

  private def fill(): Unit = {
    GreatQuestions.orderedConstructors foreach {
      constr ⇒ withinOctopus addConnection Child(constr(withinOctopus))
    }
  }
}


