package life.plenty.model

import life.plenty.model.connection.Body

trait Answer extends Space with WithParent[Space] {
  val body: String
}

class BasicAnswer(override val parent: Space, override val body: String) extends Answer {
  /** at least for now, answers do not have titles */
  override val title: String = ""
  override def preConstructor() = {
    super.preConstructor()
    addConnection(Body(body))
  }
}
