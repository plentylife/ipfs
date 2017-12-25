package life.plenty.model

case class Node(id: String)

case class Data[T](data: T) extends Node(data.toString)

object PredefinedGraph {
  val space = Node("space")
  val question = Node("question")
  /* todo. there is no need for comments. let's experiment with having only questions */
  val answer = Node("answer")
  val greatQuestion = Node("great-question")
  val why = Node("why-question")
  val who = Node("who-question")

  val connections = Set(
    Connection(space, ALLOWED_CHILD, greatQuestion),
    Connection(question, INSTANCE_OF, space), Connection(question, ALLOWED_CHILD, question),
    Connection(question, ALLOWED_CHILD, answer),
    Connection(greatQuestion, INSTANCE_OF, question),
    Connection(why, INSTANCE_OF, greatQuestion), Connection(who, INSTANCE_OF, greatQuestion)
  )
}