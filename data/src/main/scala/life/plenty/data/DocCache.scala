package life.plenty.data

import life.plenty.model.hub.definition.Hub
import rx.Var

import scala.collection.mutable


trait BasicCache[T] {
  val cache = mutable.Map[String, T]()

  def createNew(id: String): T

  /** Either creates a new doc, or gives back the existing */
  def get(hub: Hub): T = get(hub.id)

  /** Either creates a new doc, or gives back the existing */
  def get(id: String): T = {
    cache.get(id) match {
      case Some(doc) ⇒ doc
      case _ ⇒ val dw = createNew(id)
        cache += id -> dw
        dw
    }
  }
}

object DocCache extends BasicCache[DocWrapper] {
  override def createNew(id: String): DocWrapper = new DocWrapper(id)
}
