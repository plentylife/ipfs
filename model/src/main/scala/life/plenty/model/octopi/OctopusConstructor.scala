package life.plenty.model.octopi

import java.util.Date

import life.plenty.model
import life.plenty.model.connection.{Connection, CreationTime, Creator, Id}
import rx.Rx

trait OctopusConstructor {
  self: Octopus ⇒
  //  private[this] implicit val ctx: Ctx.Owner = self.ctx

  def getRxId: Rx[Option[String]] = rx.get({ case Id(id) ⇒ id })

  def id: String = getTopConnectionData({ case Id(id) ⇒ id }) getOrElse model.getHasher.b64(idGenerator)

  def idGenerator: String = {
    //    println(this, "is generating id", idProperty.getSafe, idProperty.getOrLazyElse("faulty"))
    s.exf({ case CreationTime(t) ⇒ t }).toString + s.exf({ case Creator(c) ⇒ c }).id
  }

  def getCreationTime: Rx[Option[Long]] = rx.get({ case CreationTime(t) ⇒ t })

  def getCreator: Rx[Option[User]] = rx.get({ case Creator(t) ⇒ t })

  def required: Set[Rx[Option[_]]] = Set(getCreator)

  def set(c: Connection[_]): Unit = addConnection(c)

  def asNew(properties: Connection[_]*): Unit = {
    properties.foreach(p ⇒ self.set(p))
    addConnection(CreationTime(new Date().getTime))
    for (p ← required) {
      if (p.now.isEmpty) throw new Exception(s"Class ${this} was not properly instantiated on property ${p.getClass}")
    }
  }
}
