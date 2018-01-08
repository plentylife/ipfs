package life.plenty.model

import life.plenty.model.MarkerEnum.{FILL_GREAT_QUESTIONS, HAS_FILLED_GREAT_QUESTIONS}

trait Space extends Octopus {
  val title: String

  override protected def preConstructor(): Unit = {
    super.preConstructor()
    addConnection(Title(title))
  }
}

class BasicSpace(override val title: String) extends Space {
  override protected def preConstructor(): Unit = {
    super.preConstructor()
    addConnection(Marker(FILL_GREAT_QUESTIONS))
  }
}

class AddGreatQuestions(override val withinOctopus: Space) extends ActionOnInitialize[Space] {
  override def onInitialize(): Unit = {
    println("adding great questions to ", withinOctopus, withinOctopus.connections)
    //    println(withinOctopus.connections)
    withinOctopus.getTopConnection({ case m@Marker(FILL_GREAT_QUESTIONS) ⇒ m }).foreach(_ ⇒ fill())
  }

  private def fill(): Unit = {
    // making sure that there isn't an infinite loop
    val p = withinOctopus.getTopConnectionData({ case Parent(p: Octopus) ⇒ p })
    val filled = p.exists(_.hasMarker(HAS_FILLED_GREAT_QUESTIONS)) ||
      withinOctopus.hasMarker(HAS_FILLED_GREAT_QUESTIONS)
    if (filled) {
      println("aborted gq fill")
      return
    }
    withinOctopus addConnection Marker(HAS_FILLED_GREAT_QUESTIONS)

    GreatQuestions.orderedConstructors foreach {
      constr ⇒ withinOctopus addConnection Child(constr(withinOctopus))
    }
  }
}


