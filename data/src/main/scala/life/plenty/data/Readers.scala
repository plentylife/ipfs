package life.plenty.data

import life.plenty.data
import life.plenty.model.actions.{ActionOnConnectionsRequest, ActionOnFinishDataLoad}
import life.plenty.model.connection._
import life.plenty.model.hub.GreatQuestions._
import life.plenty.model.hub._
import life.plenty.model.hub.definition.{Hub, TmpMarker}
import life.plenty.model.security.SecureUser
import rx.{Ctx, Obs, Rx, Var}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.JSON

object DbMarker extends TmpMarker

@js.native
trait JsHub extends js.Object {
  val `class`: String
  val connections: js.Array[String]
}

@js.native
trait JsDataHub extends JsHub {
  val value: String
  val valueType: String
}

class MissingDbClassLoader(loader: String) extends Exception

object DbReader {
  def ci(className: String, inst: ⇒ Hub) = {
    (cn: String) ⇒ if (className == cn) Option(inst) else None
  }

  private val availableClasses = Stream[String ⇒ Option[Hub]](
    ci("BasicUser", new BasicUser()),
    ci("ContainerSpace", new ContainerSpace()),
    ci("BasicQuestion", new BasicQuestion()),
    ci("SignupQuestion", new SignupQuestion()),
    ci("Who", new Who()),
    ci("How", new How()),
    ci("What", new What()),
    ci("Why", new Why()),
    ci("When", new When()),
    ci("Where", new Where()),
    ci("Proposal", new Proposal()),
    ci("Contribution", new Contribution()),
    ci("Vote", new Vote),
    ci("Transaction", new Transaction),
    ci("VoteAllowance", new VoteAllowance),
    ci("Members", new Members),
    ci("Event", new Event)
    //    ci("Wallet", new Wallet)
  )

  /**
    * @throws DocDoesNotExist */
  def read(id: String, doc: Option[DocWrapper] = None): Future[Hub] = {
    data.console.trace(s"Reading hub with id `${id}`")
    // from cache
    val fromCache = Cache.getOctopus(id)
    if (fromCache.nonEmpty) {
      data.console.println(s"Read ${id} from cache")
      return Future(fromCache.get)
    }

    val dbDoc = doc getOrElse new DocWrapper(id)

    val className: Future[String] = dbDoc.getData map { data ⇒
      data.`class`
    } recover {
      case e: DocDoesNotExist ⇒ console.error(s"Failed loading on ID `$id`"); throw e
    }

    className map (cName ⇒ {
      console.println(s"DbReader is constructing $cName $id")
      val potentials: Stream[Hub] = availableClasses.flatMap(f ⇒ {
        try {
          val h = f(cName)
          val res: Option[Hub] = h map { o ⇒
            o.setId(id)
            val exH = Cache.put(o) // this gives back the existing
            data.getWriterModule(exH).setDbDoc(dbDoc)
            exH
          }
          res
        } catch {
          case e: Throwable ⇒
            console.error("DBReader failed while reading a connection"); console.error(e);
            e.printStackTrace(); None
        }
      })

      potentials.headOption match {
        case None ⇒ console.error(s"Could not find loader with class ${cName}")
          throw new MissingDbClassLoader(cName)
        case Some(h) ⇒ h
      }

    })
  }

  def exists(id: String): Future[Boolean] = {
    new DocWrapper(id).exists
  }
}

object DataHubReader {
  private val leafReaders = Stream[(String, String) ⇒ Option[DataHub[_]]](
    Title(_, _), Body(_, _), Amount(_, _), Id(_, _), Name(_, _), CreationTime(_, _), Marker(_, _),
    Active(_, _), Inactive(_, _), Email(_, _)
  )

  private val hubReaders = Stream[(String, Hub) ⇒ Option[DataHub[_]]](
    Child(_, _), Parent(_, _), RootParent(_, _), Created(_, _), Creator(_, _), Contributor(_, _),
    Member(_, _), To(_, _), From(_, _), Critical(_,_)
  )

  private def readHubValue(jsHub: JsDataHub): Future[Option[DataHub[_]]] = {
    DbReader.read(jsHub.value) map {h ⇒ hubReaders flatMap {f ⇒ f(jsHub.`class`, h)} headOption}
  }
  private def readStringValue(jsHub: JsDataHub): Option[DataHub[_]] =
    leafReaders flatMap {f ⇒ f(jsHub.`class`, jsHub.value)} headOption


  def read(id: String): Future[DataHub[_]] = {
    val existing = Cache.getDataHub(id)
    if (existing.nonEmpty) {
      return Future(existing.get)
    }

    val dbDoc = new DocWrapper(id)

    dbDoc.getData flatMap {data ⇒
      val jsHub = data.asInstanceOf[JsDataHub]
      console.trace(s"DataHub Reader ${jsHub.`class`} ${jsHub.value} ${jsHub.valueType} $id")

      val constructed: Future[Option[DataHub[_]]] = jsHub.valueType match {
        case "string" ⇒ Future(readStringValue(jsHub))
        case "hub" ⇒ readHubValue(jsHub) recover {
          case e: DocDoesNotExist ⇒ console.error(e); throw e
        }
      }

      constructed foreach {r ⇒
        console.trace(s"DataHub Reader Constructed $r ${jsHub.`class`} ${jsHub.value} ${jsHub.valueType} $id")
      }

      constructed map {
        case Some(h) ⇒
          h.tmpMarker = DbMarker
          // todo. if this works, modify id getter
          h.setId(id)
          val r = Cache.put(id, h)
          setDoc(r, dbDoc)
          r
        case _ ⇒ throw new MissingDbClassLoader(jsHub.`class`)
      }
    }
  }
}

// todo create a module for user that filters out everything but transactions

class SecureUserDbReaderModule(u: SecureUser) extends DbReaderModule(u) {
  Cache.put(u)
}

/** Datahubs should load right away, to see active/inactive status */
class DbDataHubReaderModule(override val hub: DataHub[_]) extends DbReaderModule(hub) {
  override def onConnectionsRequest(): Unit = Unit

  // load once there is an id
  // fixme. was this necessary?
//  hub.onSetId {id ⇒
//    load()
//  }
}

class DbReaderModule(override val hub: Hub) extends ActionOnConnectionsRequest with
ActionOnFinishDataLoad {
  protected implicit val ctx = hub.ctx

  var instantiated = false
  val connectionsLeftToLoad = Var(-1)
//  private lazy val allCons = hub.connections.map(_.map(_.id))
  lazy val dbDoc = data.getWriterModule(hub).dbDoc // get should never trip

  val idP = Promise[Unit]()
  hub.onSetId(_ ⇒ idP.success())

  override def onConnectionsRequest(): Unit = {
//      console.println(s"Reader got request to load ${hub.getClass} with ${hub.sc.all}")
      // so since this will have to happen before the writer gets to us, we just skip the exists check

      idP.future foreach {_ ⇒ load()}
  }

  protected def load() = synchronized {
    if (!instantiated) {
      instantiated = true

      dbDoc.subscribe
      console.println(s"Reader loading ${hub} ${hub.id}")

      dbDoc.getData map { data ⇒
        val existingIds = hub.sc.all.map(_.id)
        // the reverse is important -- making sure that we are loading the oldest first
        val unloadedIds = data.connections.toList.filterNot(existingIds.contains).reverse
        connectionsLeftToLoad() = unloadedIds.size

        console.trace(s"Reader has connections to load for $hub ${hub.id} $unloadedIds")

        unloadedIds map loadConnection foreach {_ onComplete(lf ⇒ if (lf.isSuccess) {
          connectionsLeftToLoad() = connectionsLeftToLoad.now - 1
        })}
      } recover {
        case e: DocDoesNotExist ⇒ connectionsLeftToLoad() = 0
      }

      onFinishLoad {() ⇒
//        hub.loadedRx.update(true)
        hub.loadCompletePromise.success()
      }

      dbDoc.onRemoteConnectionChange(loadConnection)
    }

  }

  private def loadConnection(id: String): Future[Unit] = {
    console.trace(s"Reader trying to load connection for ${hub} ${hub.id} with id ${id}")
    val p = Promise[Unit]()
    // playing with setTimeout to allow for ui rendering
    // fixme remove
    js.timers.setTimeout(1) {

      DataHubReader.read(id) flatMap { c ⇒
        console.trace(s"Reader loaded connection for ${hub} ${c.id} -- $id")
        val f = hub.addConnection(c)
        f foreach {_ ⇒ console.trace(s"Reader added connection for ${hub} ${c.id} -- $id")}
        f
      } recover {
        case e: Throwable ⇒
          console.trace(s"Reader failed to load connection for ${hub} with id ${id}")
          console.error(e)
          e.printStackTrace()
      } onComplete(_ ⇒ p.success())

    }

    p.future
  }

  override def onFinishLoad(f: () ⇒ Unit): Unit = {
    val finishRx: Rx[Boolean] = Rx {
      console.trace(s"finish load waiting in $hub ${hub.id}")
      load() // fixme what is the point of this load?
      if (connectionsLeftToLoad() == 0) {
        console.trace(s"finish load executing in $hub ${hub.id}")
        f(); true
      } else false
    }
    finishRx.foreach(b ⇒ if (b) {finishRx.kill()})
  }
}

object DbReaderModule {
  def onFinishLoad(o: Hub, f: () ⇒ Unit) = {
    implicit val _ctx = o.ctx
    o.onModulesLoad {
      o.getTopModule({ case m: DbReaderModule ⇒ m }).foreach {
        m ⇒ m.onFinishLoad(f)
      }
    }
  }
}
