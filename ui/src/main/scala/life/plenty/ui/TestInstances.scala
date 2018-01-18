package life.plenty.ui

import life.plenty.model.connection.Child
import life.plenty.model.octopi.GreatQuestions.{When, Why}
import life.plenty.model.octopi._

object TestInstances {

  def getEntry() = {
    val frenchSpace = new BasicSpace("learning french")
    UiContext.startingSpace = frenchSpace

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
    val members = frenchSpace.getTopConnectionData({ case Child(m: Members) ⇒ m }).get


    val u1 = new BasicUser("anton")
    val u2 = new BasicUser("sarah")

    val q = new BasicQuestion(why, "lets test this")
    val c = new Contribution(q, "our first contribution", u2)
    val qw = new BasicQuestion(when, "are we meeting")
    val aw1 = new BasicAnswer(qw, "tomorrow perhaps")
    val aw2 = new BasicAnswer(qw, "monday at 3")

    members.addMember(u1)
    members.addMember(u2)

    why.addConnection(Child(q))
    qw.addConnection(Child(aw1))
    qw.addConnection(Child(aw2))
    when.addConnection(Child(qw))
    q.addConnection(Child(c))

    println(when)
    frenchSpace
  }

}

