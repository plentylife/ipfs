package life.plenty.ui.display

import life.plenty.model.connection.Child
import life.plenty.model.octopi.{Answer, GreatQuestion, Octopus}
import life.plenty.ui
import life.plenty.ui.display.actions.CreateAnswer
import life.plenty.ui.display.meta.{GroupedChildDisplay, GroupedModularDisplay}
import life.plenty.ui.model.DisplayModel.DisplayModule

class GreatQuestionGroup(private val _withinOctopus: Octopus) extends GroupedChildDisplay(_withinOctopus) {
  override protected val displayInOrder: List[String] = List("great", "other")

  override def doDisplay() = {
    withinOctopus.connections.collectFirst({ case Child(_: GreatQuestion) ⇒ Unit }).nonEmpty
  }

  override protected def groupBy(o: Octopus): String = o match {
    case o: GreatQuestion ⇒ "great"
    case _ ⇒ "other"
  }
}

class AnswerGroup(private val _withinOctopus: Octopus) extends GroupedChildDisplay(_withinOctopus) {
  override protected val displayInOrder: List[String] = List("answers", "other")

  override def doDisplay() = {
    //    val r = withinOctopus.connections.collectFirst({ case Child(_: Answer) ⇒ Unit }).nonEmpty
    val r = true
    ui.console.println(s"AnswerGroup doDisplay $r")
    r
  }

  override protected def groupBy(o: Octopus): String = o match {
    case o: Answer ⇒ "answers"
    case _ ⇒ "other"
  }
}

class QuestionModuleGroup(private val _withinOctopus: Octopus) extends GroupedModularDisplay(_withinOctopus) {
  override protected val displayInOrder: List[String] = List("question-nav", "other")

  //  override def doDisplay() = true

  override protected def groupBy(m: DisplayModule[_]): String = m match {
    case (_: QuestionTitle | _: CreateAnswer) ⇒ "question-nav"
    case _ ⇒ "other"
  }
}
