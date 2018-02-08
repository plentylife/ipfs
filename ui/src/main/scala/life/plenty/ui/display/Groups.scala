package life.plenty.ui.display

import life.plenty.model.octopi._
import life.plenty.model.octopi.definition.Octopus
import life.plenty.ui.display.actions.CreateAnswer
import life.plenty.ui.display.meta.{GroupedChildDisplay, GroupedModularDisplay}
import life.plenty.ui.model.DisplayModel.DisplayModule
import life.plenty.ui.model.UiContext

class TopSpaceGroups(private val _withinOctopus: Octopus) extends GroupedChildDisplay(_withinOctopus) {
  override protected val displayInOrder: List[String] = List("other", "event", "great")
  override protected val titles: Map[String, String] = Map("great" → "Planning", "event" → "Events")

  override def doDisplay() = {
    // fixme the second condition is temporary
    UiContext.startingSpace.value.get.id == _withinOctopus.id && !_withinOctopus.isInstanceOf[Question]
  }

  override protected def groupBy(o: Octopus): String = o match {
    case o: GreatQuestion ⇒ "great"
    case o: Event ⇒ "event"
    case _ ⇒ "other"
  }
}

class AnswerGroup(private val _withinOctopus: Octopus) extends GroupedChildDisplay(_withinOctopus) {
  override protected val displayInOrder: List[String] = List("answers", "other")

  override def doDisplay() = true

  override protected def groupBy(o: Octopus): String = o match {
    case o: Answer ⇒ "answers"
    case _ ⇒ "other"
  }
}

class QuestionModuleGroup(private val _withinOctopus: Octopus) extends GroupedModularDisplay(_withinOctopus) {
  override protected val displayInOrder: List[String] = List("question-nav", "other")

  override def doDisplay() = {
    //    println(s"Question module group starting ${UiContext.startingSpace.value.get.id} ${_withinOctopus.id}")
    UiContext.startingSpace.value.map(_.id).getOrElse("") != _withinOctopus.id
  }

  override protected def groupBy(m: DisplayModule[_]): String = m match {
    case (_: QuestionTitle | _: CreateAnswer) ⇒ "question-nav"
    case _ ⇒ "other"
  }
}
