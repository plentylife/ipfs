package life.plenty.model

import life.plenty.model.connection.Marker
import life.plenty.model.connection.MarkerEnum.FILL_GREAT_QUESTIONS

trait Question extends Space with WithParent[Space]

class BasicQuestion(override val parent: Space, override val title: String) extends Question {
  override def preConstructor(): Unit = {
    super.preConstructor()
    //println("BasicQuestion constr", this.connections)
  }
}

class BasicSpace(override val title: String) extends Space {
  override protected def preConstructor(): Unit = {
    super.preConstructor()
    addConnection(Marker(FILL_GREAT_QUESTIONS))
    //println("basic space init", this.connections)
  }
}
