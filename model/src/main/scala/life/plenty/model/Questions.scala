package life.plenty.model

trait WithAuthor {
  val author: User
}

trait CanBeSpace

trait Question extends CanBeSpace {
  val question: String
  // some question could have been created in a previous event, and thus should be reused
  val parentQuestionDefinition: Option[Question]
  // but this particular question is in a different space
  val parentSpace: CanBeSpace
  val canHaveComments: Boolean
}

trait GreatQuestion extends Question {
  override val canHaveComments = false
}

object Why

