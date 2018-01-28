package life.plenty.model.octopi

import java.util.Date

import life.plenty.model
import life.plenty.model.actions.ActionOnNew
import life.plenty.model.connection._
import life.plenty.model.utils._
import rx.{Rx, Var}

import scala.util.Random

trait OctopusConstructor {
  self: Octopus ⇒

  private val rand = Random

  def getRxId: Rx[Option[String]] = rx.get({ case Id(id) ⇒ id })

  /** Either retrieves the id, or generates a new one, and sets it */
  def id: String = getTopConnectionData({ case Id(id) ⇒ id }) getOrElse {
    val gid = model.getHasher.b64(generateId)
    set(Id(gid))
    gid
  }

  private def generateId: String = {
    val res = rand.nextLong().toString +
      s.exf({ case CreationTime(t) ⇒ t }).toString + s.exf({ case Creator(c) ⇒ c }).id
    res
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
    println(s"attempting to instantiate ${this.getClass} with creator ${model.defaultCreator}")
    properties.foreach(p ⇒ {
      p.tmpMarker = AtInstantiation
      self.set(p)
    })
    val ct = CreationTime(new Date().getTime)
    ct.tmpMarker = AtInstantiation
    addConnection(ct)

    model.defaultCreator.foreach(c ⇒ set(Creator(c).inst))
    for (p ← required) {
      if (p.now.isEmpty) throw new Exception(s"Class ${this.getClass} was not properly instantiated. " +
        s"Connections ${this._connections.now}")
    }

    getCreator.addConnection(Created(this).inst)
    isNewVar() = true

    getModules({ case m: ActionOnNew[_] ⇒ m }).foreach({_.onNew()})
    println(s"successfully instantiated ${this} ${this.id}")
  }
}
