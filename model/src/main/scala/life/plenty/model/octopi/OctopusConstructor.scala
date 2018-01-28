package life.plenty.model.octopi

import java.util.Date

import life.plenty.model
import life.plenty.model.connection._
import life.plenty.model.utils._
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

  /** alias for [[addConnection()]] */
  def set(c: Connection[_]): Unit = addConnection(c)

  private lazy val isNewVar = Var(false)

  def isNew = isNewVar.now

  def onNew(f: ⇒ Unit): Unit = {
    isNewVar.foreach(i ⇒ {
      if (i) {
        f
        isNewVar.kill()
      }
    })
  }

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

    getCreator.addConnection(Created(this).inst)
    isNewVar() = true
    println(s"successfully instantiated ${this} ${this.id}")
  }
}
