package life.plenty.model

trait Node {
  val id: String
}

case class EmptyNode(id: String) extends Node

case class Data[T](data: T) extends Node {
  override val id = data.toString
}

object PredefinedGraph {
  val space = EmptyNode("space")
  val question = EmptyNode("question")
  /* todo. there is no need for comments. let's experiment with having only questions */
  val answer = EmptyNode("answer")
  val greatQuestion = EmptyNode("great-question")
  val why = EmptyNode("why-question")
  val who = EmptyNode("who-question")

  val connections = Set(
    Connection(space, ALLOWED_CHILD, greatQuestion),
    Connection(question, INSTANCE_OF, space), Connection(question, ALLOWED_CHILD, question),
    Connection(question, ALLOWED_CHILD, answer),
    Connection(greatQuestion, INSTANCE_OF, question),
    Connection(why, INSTANCE_OF, greatQuestion), Connection(who, INSTANCE_OF, greatQuestion)
  )
}