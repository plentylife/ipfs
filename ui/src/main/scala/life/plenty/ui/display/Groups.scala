package life.plenty.ui.display

import life.plenty.model.connection.Child
import life.plenty.model.octopi.{GreatQuestion, Octopus}

class GreatQuestionGroup(private val _withinOctopus: Octopus) extends GroupedChildDisplay(_withinOctopus) {
  override protected val displayInOrder: List[String] = List("great", "other")

  override def doDisplay() = {
    println("do display", withinOctopus.connections)
    withinOctopus.connections.collectFirst({ case Child(_: GreatQuestion) ⇒ Unit }).nonEmpty
  }

  override protected def groupBy(o: Octopus): String = o match {
    case o: GreatQuestion ⇒ "great"
    case _ ⇒ "other"
  }
}
