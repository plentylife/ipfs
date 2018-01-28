package life.plenty.model.octopi

import java.util.Date

import life.plenty.model
import life.plenty.model.connection._
import rx.{Rx, Var}

trait OctopusConstructor {
  self: Octopus ⇒

  def getRxId: Rx[Option[String]] = rx.get({ case Id(id) ⇒ id })

  def id: String = getTopConnectionData({ case Id(id) ⇒ id }) getOrElse model.getHasher.b64(idGenerator)

  def idGenerator: String = {
    //    println(s"${this.getClass} is generating id; ${_connections.now}")
    s.exf({ case CreationTime(t) ⇒ t }).toString + s.exf({ case Creator(c) ⇒ c }).id
  }

  def getCreationTime: Rx[Option[Long]] = rx.get({ case CreationTime(t) ⇒ t })

  def getCreator: Rx[Option[User]] = rx.get({ case Creator(t) ⇒ t })

  def required: Set[Rx[Option[_]]] = Set(getCreator)

  def set(c: Connection[_]): Unit = addConnection(c)

  val instantiated = Var(false)

  def onInstantiate(f: ⇒ Unit): Unit = instantiated.foreach(i ⇒ if (i) f)

  def asNew(properties: Connection[_]*): Unit = {
    println(s"attempting to instantiate ${this.getClass}")
    properties.foreach(p ⇒ {
      p.tmpMarker = AtInstantiation
      self.set(p)
    })
    val ct = CreationTime(new Date().getTime)
    ct.tmpMarker = AtInstantiation
    addConnection(ct)
    for (p ← required) {
      if (p.now.isEmpty) throw new Exception(s"Class ${this.getClass} was not properly instantiated. " +
        s"Connections ${this._connections.now}")
    }
    instantiated() = true
    println(s"successfully instantiated ${this} ${this.id}")
  }
}
