package life.plenty.model.actions

import life.plenty.model.connection.{Connection, Parent}
import life.plenty.model.octopi.GreatQuestions._
import life.plenty.model.octopi.{GreatQuestion, Space}

/* fixme move*/
class AddGreatQuestions(override val withinOctopus: Space) extends ActionOnNew[Space] {
  override def onNew(): Unit = {
    println("Filling great questions")
    fill()
  }

  private def fill(): Unit = {

    println("Adding great questions")
    addIfNotExists(_.value.isInstanceOf[Who], new Who)
    addIfNotExists(_.value.isInstanceOf[Why], new Why)
    addIfNotExists(_.value.isInstanceOf[What], new What)
    addIfNotExists(_.value.isInstanceOf[Where], new Where)
    addIfNotExists(_.value.isInstanceOf[When], new When)
    addIfNotExists(_.value.isInstanceOf[How], new How)

  }

  private def addIfNotExists(check: Connection[_] ⇒ Boolean, constr: GreatQuestion) = {
    if (!withinOctopus.connections.exists(check)) {
      constr.asNew(Parent(withinOctopus))
    }
  }
}
