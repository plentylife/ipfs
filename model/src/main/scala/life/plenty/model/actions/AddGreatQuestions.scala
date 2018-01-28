package life.plenty.model.actions

import life.plenty.model.connection.{Connection, Parent}
import life.plenty.model.octopi.GreatQuestions.Why
import life.plenty.model.octopi.{GreatQuestion, Space}

/* fixme move*/
class AddGreatQuestions(override val withinOctopus: Space) extends ActionOnNew[Space] {
  override def onNew(): Unit = {
    println("Filling great questions")
    fill()
    //    withinOctopus.getTopConnection({ case m@Marker(FILL_GREAT_QUESTIONS) ⇒ m }).foreach(_ ⇒ fill())
    //    println("added great questions to ", withinOctopus, withinOctopus.connections)
  }

  private def fill(): Unit = {
    //    // making sure that there isn't an infinite loop
    //    val p = withinOctopus.getTopConnectionData({ case Parent(p: Octopus) ⇒ p })
    //    val filled = p.exists(_.hasMarker(HAS_FILLED_GREAT_QUESTIONS)) ||
    //      withinOctopus.hasMarker(HAS_FILLED_GREAT_QUESTIONS)
    //    if (filled) {
    //      //println("aborted gq fill")
    //      return
    //    }
    //    withinOctopus addConnection Marker(HAS_FILLED_GREAT_QUESTIONS)

    println("Adding great questions")
    addIfNotExists(_.value.isInstanceOf[Why], new Why)
    //    addIfNotExists(_.value.isInstanceOf[Where], new Where)
    //    addIfNotExists(_.value.isInstanceOf[When], new When)
    //    addIfNotExists(_.value.isInstanceOf[Who], new Who)

    // reverse because first in -- last out
    // fixme. uncomment
    //        List(new Why, new Where, new When).reverse map {
    //          o ⇒ o.asNew()
    //        } foreach {
    //          question ⇒
    //            if (!withinOctopus.connections.collect(
    //              { case Child(q: GreatQuestion) ⇒
    //                //println(q.getClass, question.getClass)
    //                q
    //              }
    //            ).exists(_.getClass == question.getClass)) withinOctopus addConnection Child(question)
    //        }
  }

  private def addIfNotExists(check: Connection[_] ⇒ Boolean, constr: GreatQuestion) = {
    if (!withinOctopus.connections.exists(check)) {
      constr.asNew(Parent(withinOctopus))
    }
  }
}
