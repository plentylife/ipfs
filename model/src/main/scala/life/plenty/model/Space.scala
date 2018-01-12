package life.plenty.model

import life.plenty.model.connection.Title

trait Space extends Octopus {
  val title: String

  override protected def preConstructor(): Unit = {
    super.preConstructor()
    addConnection(Title(title))
  }
}




