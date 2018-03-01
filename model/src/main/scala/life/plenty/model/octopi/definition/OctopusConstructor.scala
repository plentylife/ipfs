package life.plenty.model.octopi.definition

import java.util.Date

import scala.concurrent.ExecutionContext.Implicits.global
import life.plenty.model
import life.plenty.model.actions.ActionOnNew
import life.plenty.model.connection._
import life.plenty.model.octopi.User
import life.plenty.model.utils._
import rx.{Rx, Var}

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Random, Success}

trait OctopusConstructor {
  self: Hub ⇒

  //  implicit private[this] val ctxConstructor: Ctx.Owner = self.ctx

  protected val rand = Random

  def getRxId: Rx[Option[String]] = rx.get({ case Id(id) ⇒ id })

  /** Either retrieves the id, or generates a new one, and sets it */
  def id: String = sc.ex({ case Id(id) ⇒ id }) getOrElse {
    val gid = generateId
    setInit(Id(gid))
    gid
  }

  def setId(_id: String) = {
    addConnectionForced(Id(_id).inst)
    onSetIdFunctions foreach {f ⇒ f(_id)}
    onSetIdFunctions = Nil
  }

  private var onSetIdFunctions = List[String ⇒ Unit]()
  def onSetId(f: String ⇒ Unit) = onSetIdFunctions +:= f

  protected def generateId: String = {
    val parentOrCreator: Hub = sc.ex({case Parent(p: Hub) ⇒ p}).getOrElse(sc.exf({ case Creator(c) ⇒ c }))
    generateId(sc.exf({ case CreationTime(t) ⇒ t }), parentOrCreator.id)
  }

  protected def generateId(time: Long, parentalId: String): String = {
    // the length is slightly different from the datahubs, thus the ids are in separate spaces
    val cid = parentalId.substring(parentalId.length - 5, parentalId.length)
    val hid = model.getHasher.b64(rand.nextLong().toString + time.toString).substring(0, 6)
    cid + hid
  }

  lazy val getCreationTime: Rx[Option[Long]] = rx.get({ case CreationTime(t) ⇒ t })

  lazy val getCreator: Rx[Option[User]] = rx.get({ case Creator(t) ⇒ t })

  private var _required: Set[() ⇒ Rx[Option[_]]] = Set(() ⇒ getCreator)

  def addToRequired(r: ⇒ Rx[Option[_]]) = _required += { () ⇒ r }

  def clearRequired() = _required = Set()

  final def required: Set[() ⇒ Rx[Option[_]]] = _required


  private var initialConnectionCompletion = Future[Unit]{}
  /** alias for [[addConnection()]] with the connection marked and that tracks the completion of connections */
  private def setInit(c: DataHub[_]): Unit = {
    val f = addConnection(c.inst)
    initialConnectionCompletion = initialConnectionCompletion.flatMap(_ ⇒ f)
  }

  private lazy val isNewVar = Var(false)

  def isNew = isNewVar.now

  /** this is not called for DataHubs */
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

  def asNew(_properties: DataHub[_]*): Future[Unit] = {
    model.console.trace(s"attempting to instantiate ${this.getClass} with creator ${model.defaultCreator}")

    // has to be first for purposes of creating ids
    val ct = CreationTime(new Date().getTime)
    val cc: Option[Creator] = _properties.find(_.isInstanceOf[Creator]).map(_.asInstanceOf[Creator]).orElse({
      model.defaultCreator.map(c ⇒ Creator(c))
    })
    val pp = _properties.collectFirst({case Parent(p: Hub) ⇒ p})

    if (sc.ex({case i: Id ⇒ i}).isEmpty) { // setting id, only if there's not one already
      val idProp = _properties.collectFirst({ case Id(i) ⇒ i })
      if (idProp.isEmpty) {
        // in this case there must be a creator or parent
        val parentalId = pp.orElse(cc).map(_.id).get
        setId(generateId(ct.value, parentalId))
      } else setId(idProp.get)

      if (cc.isEmpty) {
        model.console.error(s"Warning: no creator or parent was set for ${this.getClass}")
      }
    }

    self.setInit(ct)
    cc foreach self.setInit

    val properties = _properties.filterNot(_.isInstanceOf[Id])
    properties.foreach(p ⇒ {
      p.tmpMarker = AtInstantiation
      self.setInit(p)
    })

    val finished = Promise[Unit]()
    initialConnectionCompletion onComplete {
      case Success(_) ⇒
        model.console.trace(s"New hub [${this.id}] has connections ${connections.now}")

        for (p ← required) {
          if (p().now.isEmpty) {
            val msg = s"Class ${this.getClass} was not properly instantiated. " +
              s"Connections ${this._connections.now}"
            model.console.error(msg)
            throw new Exception(msg)
          }
        }

        getCreator.addConnection(Created(this).inst)
        isNewVar() = true

        getModules({ case m: ActionOnNew[_] ⇒ m }).foreach({_.onNew()})
        model.console.println(s"successfully instantiated ${this} ${this.id}")

        finished.success()
      case Failure(e) ⇒
        model.console.error(s"Some of the connections failed to load on instantiation of ${this.getClass}")
        model.console.error(e)
        e.printStackTrace()
        finished.failure(e)
    }
    finished.future
  }
}
