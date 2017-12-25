package life.plenty.model

object PredefinedGraph {
  val space = EmptyNode("space")
  val question = EmptyNode("question")
  /* todo. there is no need for comments. let's experiment with having only questions */
  val answer = EmptyNode("answer")
  val greatQuestion = EmptyNode("great-question")
  val why = EmptyNode("why-question")
  val who = EmptyNode("who-question")

  val predefinedConnections = Set(
    Connection(space, ALLOWED_CHILD, greatQuestion),
    Connection(question, INSTANCE_OF, space), Connection(question, ALLOWED_CHILD, question),
    Connection(question, ALLOWED_CHILD, answer),
    Connection(greatQuestion, INSTANCE_OF, question),
    Connection(why, INSTANCE_OF, greatQuestion), Connection(who, INSTANCE_OF, greatQuestion)
  )
}
