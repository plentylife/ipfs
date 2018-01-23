package life.plenty.model.octopi

import life.plenty.model.connection.Title

trait Space extends Octopus {
  protected[Space] val _title: String = null
  lazy val title = new Property[String]({ case Title(t: String) ⇒ t }, this)

  override def idGenerator: String = title()

  override protected def preConstructor(): Unit = {
    super.preConstructor()
    title.initWith(_title)
    title forInit { t ⇒ addConnection(Title(t)) }
  }
}




