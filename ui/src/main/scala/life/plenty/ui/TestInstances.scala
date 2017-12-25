package life.plenty.ui

import life.plenty.model._

object TestInstances {

  import PredefinedGraph._

  val testSpace = Data("weekly food munchers")
  val firstQuestion = Data("going to bring snacks")
  val secondQuestion = Data("are we doing this at all")

  val connections = Set[Connection[_]](
    Connection(testSpace, USER_CREATED_INSTANCE_OF, space),
    Connection(firstQuestion, USER_CREATED_INSTANCE_OF, who), Connection(firstQuestion, INSIDE_SPACE_OF, testSpace),
    Connection(secondQuestion, USER_CREATED_INSTANCE_OF, why), Connection(secondQuestion, INSIDE_SPACE_OF, testSpace)
  )
}

