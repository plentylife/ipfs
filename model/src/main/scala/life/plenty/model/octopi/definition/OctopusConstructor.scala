package life.plenty.model.octopi.definition

import java.util.Date

import life.plenty.model
import life.plenty.model.actions.ActionOnNew
import life.plenty.model.connection._
import life.plenty.model.octopi.User
import life.plenty.model.utils._
import rx.{Rx, Var}

import scala.util.Random

trait OctopusConstructor {
  self: Hub ⇒

  //  implicit private[this] val ctxConstructor: Ctx.Owner = self.ctx

  protected val rand = Random

  def getRxId: Rx[Option[String]] = rx.get({ case Id(id) ⇒ id })

  /** Either retrieves the id, or generates a new one, and sets it */
  def id: String = sc.ex({ case Id(id) ⇒ id }) getOrElse {
    val gid = model.getHasher.b64(generateId)
    setInit(Id(gid))
    gid
  }

  protected def generateId: String = {
    generateId(sc.exf({ case CreationTime(t) ⇒ t }), sc.exf({ case Creator(c) ⇒ c }).id)
  }

  protected def generateId(time: Long, creatorId: String): String = {
    rand.nextLong().toString + time.toString + creatorId
  }

  lazy val getCreationTime: Rx[Option[Long]] = rx.get({ case CreationTime(t) ⇒ t })

  lazy val getCreator: Rx[Option[User]] = rx.get({ case Creator(t) ⇒ t })

  private var _required: Set[() ⇒ Rx[Option[_]]] = Set(() ⇒ getCreator)

  def addToRequired(r: ⇒ Rx[Option[_]]) = _required += { () ⇒ r }

  def clearRequired() = _required = Set()

  final def required: Set[() ⇒ Rx[Option[_]]] = _required

  /** alias for [[addConnection()]] with the connection marked */
  def setInit(c: DataHub[_]): Unit = addConnection(c.inst)

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

  def onModulesLoad(f: ⇒ Unit): Unit = {
    modulesFinishedLoading.foreach(i ⇒ {
      if (i) {
        model.console.println("Modules finished loading. Executing function.")
        f
        modulesFinishedLoading.kill()
      }
    })
  }

  def asNew(properties: DataHub[_]*): Unit = {
    model.console.trace(s"attempting to instantiate ${this.getClass} with creator ${model.defaultCreator}")

    // has to be first for purposes of creating ids
    val ct = CreationTime(new Date().getTime)
    val cc: Option[Creator] = properties.find(_.isInstanceOf[Creator]).map(_.asInstanceOf[Creator]).orElse({
      model.defaultCreator.map(c ⇒ Creator(c))
    })
    val idProp = properties.find(_.isInstanceOf[Id])

    if (idProp.isEmpty) {
      // in this case there must be a creator
      setInit(Id(generateId(ct.value, cc.map(_.user.id).get)))
    } else setInit(idProp.get)

    model.console.error(s"Warning: no creator was set for ${this.getClass}")

    self.setInit(ct)
    cc foreach self.setInit

    properties.foreach(p ⇒ {
      p.tmpMarker = AtInstantiation
      self.setInit(p)
    })
    model.console.trace("New octopus has connections")

    for (p ← required) {
      if (p().now.isEmpty) throw new Exception(s"Class ${this.getClass} was not properly instantiated. " +
        s"Connections ${this._connections.now}")
    }

    getCreator.addConnection(Created(this).inst)
    isNewVar() = true

    getModules({ case m: ActionOnNew[_] ⇒ m }).foreach({_.onNew()})
    model.console.println(s"successfully instantiated ${this} ${this.id}")
  }
}
