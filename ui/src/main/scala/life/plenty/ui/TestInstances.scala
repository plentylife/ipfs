package life.plenty.ui

import life.plenty.model.connection.Child
import life.plenty.model.octopi.GreatQuestions.{When, Why}
import life.plenty.model.octopi.{BasicAnswer, BasicQuestion, BasicSpace, Contribution}

object TestInstances {

  def getEntry() = {
    val frenchSpace = new BasicSpace("learning french")
    val why = frenchSpace.getTopConnectionData({ case Child(q: Why) ⇒ q }) getOrElse {
      val q = new Why(frenchSpace)
      frenchSpace.addConnection(Child(q))
      q
    }
    val when = frenchSpace.getTopConnectionData({ case Child(q: When) ⇒ q }) getOrElse {
      val q = new When(frenchSpace)
      frenchSpace.addConnection(Child(q))
      q
    }
    val q = new BasicQuestion(why, "lets test this")
    val c = new Contribution(q, "our first contribution")
    val a = new BasicAnswer(when, "tomorrow perhaps")

    why.addConnection(Child(q))
    when.addConnection(Child(a))
    q.addConnection(Child(c))

    println(when)
    frenchSpace
  }

}

