package life.plenty.model.utils

import life.plenty.model.actions.PropertyWatch
import life.plenty.model.connection.Connection
import life.plenty.model.octopi.Octopus

/** the get on connection data is not safe
  *
  * @param init can be null */
class Property[T](val getter: PartialFunction[Connection[_], T], val in: Octopus, val init: T = null) {
  private var _inner: Option[T] = Option(init)

  def setInner(v: T): Unit = _inner = Option(v)

  def applyInner(f: (T) ⇒ Unit): Unit = {
    println("trying to apply inner to ", _inner)
    _inner foreach f
    //    println("applied")
  }

  def apply(): T = {
    try {
      getSafe.get
    } catch {
      case e: Throwable ⇒ println(e.getMessage); e.printStackTrace(); throw e
    }
  }

  def getSafe: Option[T] = _inner orElse in.getTopConnectionData(getter)

  def map[B](f: (T) ⇒ B): Option[B] = getSafe map f

  def getOrLazyElse(v: ⇒ T): T = getSafe.getOrElse(v)

  private var updaters = Set[() ⇒ Unit]()

  def registerUpdater(f: () ⇒ Unit) = updaters += f

  def update(c: Connection[_]): Unit = {
    if (getter.isDefinedAt(c)) setInner(getter(c))
    updaters foreach (f ⇒ f())
  }

  /* Constructor */
  //  println("adding property watch module")
  in.addModule(new PropertyWatch[T](in, this))
}
