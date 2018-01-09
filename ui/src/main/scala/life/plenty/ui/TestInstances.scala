package life.plenty.ui

import life.plenty.model.GreatQuestions.Why
import life.plenty.model.connection.Child
import life.plenty.model.{BasicQuestion, BasicSpace}

object TestInstances {

  def getEntry() = {
    val frenchSpace = new BasicSpace("learning french")
    val why = frenchSpace.getTopConnectionData({ case Child(q: Why) ⇒ q }).get
    val q = new BasicQuestion(why, "lets test this")
    why.addConnection(Child(q))

    frenchSpace
  }

}

