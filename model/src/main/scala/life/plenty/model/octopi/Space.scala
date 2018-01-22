package life.plenty.model.octopi

import life.plenty.model.connection.Title

trait Space extends Octopus {
  val title: String

  override def id: String = title

  override protected def preConstructor(): Unit = {
    super.preConstructor()
    addConnection(Title(title))
  }
}




