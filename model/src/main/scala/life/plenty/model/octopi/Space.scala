package life.plenty.model.octopi

import life.plenty.model.connection.Title

trait Space extends Octopus {
  protected val _title: String
  lazy val title = new Property[String]({ case Title(t: String) ⇒ t }, this, _title)

  override def idGenerator: String = super.idGenerator + title()

  override protected def preConstructor(): Unit = {
    super.preConstructor()
    title.setInner(_title)
    title applyInner { t ⇒ addConnection(Title(t)) }
  }
}




