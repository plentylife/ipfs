package life.plenty.model.octopi

import life.plenty.model.connection.Title

trait Space extends Octopus {
  protected[Space] val _title: String = null
  lazy val title = new Property[String]({ case Title(t: String) ⇒ t }, this)

  override def idGenerator: String = title()

  override protected def preConstructor(): Unit = {
    super.preConstructor()
    title.setInner(_title)
    title forInner { t ⇒ addConnection(Title(t)) }
  }
}




