package life.plenty.data

import life.plenty.data
import life.plenty.model.actions.{ActionOnConnectionsRequest, ActionOnFinishDataLoad}
import life.plenty.model.connection._
import life.plenty.model.octopi.GreatQuestions._
import life.plenty.model.octopi._
import life.plenty.model.octopi.definition.{Hub, TmpMarker}
import life.plenty.model.security.SecureUser
import rx.{Rx, Var}

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

    className flatMap (cName ⇒ {
      console.println(s"DbReader is constructing $cName")
      val potentials: Stream[Future[Hub]] = availableClasses.flatMap(f ⇒ {
        try {
          val h = f(cName)
          val res: Option[Future[Hub]] = h map { o ⇒
            val idCon = Id(id)
            idCon.tmpMarker = DbMarker

            // have to wait on id!!
            o.addConnection(idCon) map {_ ⇒
              val exH = Cache.put(o) // this gives back the existing
              data.getWriterModule(exH).setDbDoc(dbDoc)
              exH
            }
          }
          res
        } catch {
          case e: Throwable ⇒ console.error(e); e.printStackTrace(); None
        }
      })

      potentials.headOption match {
        case None ⇒ console.error(s"Could not find loader with class ${cName}")
          throw new MissingDbClassLoader(cName)
        case Some(f) ⇒ f
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
    Member(_, _), To(_, _), From(_, _)
  )

  private def readHubValue(jsHub: JsDataHub): Future[Option[DataHub[_]]] = {
    DbReader.read(jsHub.value) map {h ⇒ hubReaders flatMap {f ⇒ f(jsHub.`class`, h)} headOption}
  }
  private def readStringValue(jsHub: JsDataHub): Option[DataHub[_]] =
    leafReaders flatMap {f ⇒ f(jsHub.`class`, jsHub.value)} headOption


  def read(id: String): Future[DataHub[_]] = {
    val dbDoc = new DocWrapper(id)

    dbDoc.getData flatMap {data ⇒
      val jsHub = data.asInstanceOf[JsDataHub]
      console.trace(s"DataHub Reader ${jsHub.`class`} ${jsHub.value} ${jsHub.valueType} $id")

      val constructed: Future[Option[DataHub[_]]] = jsHub.valueType match {
        case "string" ⇒ Future(readStringValue(jsHub))
        case "hub" ⇒ try {readHubValue(jsHub)} catch {
          case e: DocDoesNotExist ⇒ console.error(e); throw e
        }
      }

      constructed map {
        case Some(h) ⇒
          h.tmpMarker = DbMarker
          h
        case _ ⇒ throw new MissingDbClassLoader(jsHub.`class`)
      }
    }
  }
}

// todo create a module for user that filters out everything but transactions

class SecureUserDbReaderModule(u: SecureUser) extends DbReaderModule(u) {
  Cache.put(u)
}

class DbReaderModule(override val hub: Hub) extends ActionOnConnectionsRequest with
ActionOnFinishDataLoad {
  private implicit val ctx = hub.ctx

  var instantiated = false
  val connectionsLeftToLoad = Var(-1)
//  private lazy val allCons = hub.connections.map(_.map(_.id))
  lazy val dbDoc = data.getWriterModule(hub).dbDoc // get should never trip

  override def onConnectionsRequest(): Unit = synchronized {
    if (!instantiated) {
      instantiated = true
      console.println(s"Reader got request to load ${hub.getClass} with ${hub.sc.all}")
      dbDoc.exists foreach {ex ⇒ if (ex) load()}
    }
  }

  private def load() = {
    dbDoc.subscribe
    console.println(s"Reader loading ${hub} ${hub.id}")

    dbDoc.getData map { data ⇒
      val existingIds = hub.connections.now.map(_.id)
      val unloadedIds = data.connections.toList.filterNot(existingIds.contains)
      connectionsLeftToLoad() = unloadedIds.size + connectionsLeftToLoad.now

      console.trace(s"Reader has connections to load for $hub ${hub.id} $unloadedIds")

      unloadedIds map loadConnection foreach {_ onComplete(lf ⇒ if (lf.isSuccess) {
        connectionsLeftToLoad() = connectionsLeftToLoad.now - 1
      })}
    }

    dbDoc.onRemoteConnectionChange(loadConnection)
  }

  private def loadConnection(id: String): Future[Unit] = {
    DataHubReader.read(id) flatMap { c ⇒
      console.trace(s"Reader loaded connection for ${hub} ${c.id}")
      hub.addConnection(c)
    } recover {
      case e: Throwable ⇒
        console.trace(s"Reader failed to load connection for ${hub} with id ${id}")
        console.error(e)
    }
  }

  override def onFinishLoad(f: () ⇒ Unit): Unit = {
    val finishRx: Rx[Boolean] = Rx {
      if (connectionsLeftToLoad() == 0) {
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
