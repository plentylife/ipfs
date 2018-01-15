package life.plenty.ui

import life.plenty.model.GreatQuestions.Why
import life.plenty.model.connection.Child
import life.plenty.model.{BasicQuestion, BasicSpace, Contribution}

object TestInstances {

  def getEntry() = {
    val frenchSpace = new BasicSpace("learning french")
    val why = frenchSpace.getTopConnectionData({ case Child(q: Why) ⇒ q }) getOrElse {
      val q = new Why(frenchSpace)
      frenchSpace.addConnection(Child(q))
      q
    }
    val q = new BasicQuestion(why, "lets test this")
    val c = new Contribution(q, "our first contribution")

    why.addConnection(Child(q))
    q.addConnection(Child(c))

    frenchSpace
  }

}

