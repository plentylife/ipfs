package life.plenty.ui

import life.plenty.model._

object TestInstances {

  import PredefinedGraph._

  val testSpace = Data("weekly food munchers")
  val firstQuestion = Data("going to bring snacks")
  val secondQuestion = Data("are we doing this at all")

  val connections = Set(
    Connection(testSpace, USER_CREATED_INSTANCE_OF, space),
    Connection(firstQuestion, INSTANCE_OF, question), Connection(firstQuestion, INSIDE_SPACE_OF, testSpace),
    Connection(secondQuestion, INSTANCE_OF, question), Connection(secondQuestion, INSIDE_SPACE_OF, testSpace)
  )
}

