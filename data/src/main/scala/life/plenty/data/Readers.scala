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
}

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

  def read(id: String): Future[Hub] = {
    data.console.trace(s"Reading hub with id `${id}`")
    // from cache
    val fromCache = Cache.getOctopus(id)
    if (fromCache.nonEmpty) {
      data.console.println(s"Read ${id} from cache")
      return Future(fromCache.get)
    }

    val dbDoc = new AsyncShareDoc(id, true)

    val className: Future[String] = dbDoc.getData map { data ⇒
      data.`class`
    } recover {
      case e: DocDoesNotExist ⇒ console.error(s"Failed loading on ID `$id`"); throw e
    }

    className flatMap (cName ⇒ {
      console.println(s"DbReader is constructing $cName")
      val potentials: Stream[Option[Future[Hub]]] = availableClasses.map(f ⇒ {
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

      potentials.flatten.headOption match {
        case None ⇒ console.error(s"Could not find loader with class ${cName}")
          throw new Exception(s"Classloader for $cName not found")
        case Some(f) ⇒ f
      }

    })
  }

  def exists(id: String): Future[Boolean] = {
    new AsyncShareDoc(id).exists
  }
}


object ConnectionReader {
  private val leafReaders = Stream[(String, String) ⇒ Option[DataHub[_]]](
    Title(_, _), Body(_, _), Amount(_, _), Id(_, _), Name(_, _), CreationTime(_, _), Marker(_, _),
    Active(_, _), Inactive(_, _), Email(_, _)
  )

  private val octopusReaders = Stream[(String, Hub) ⇒ Option[DataHub[_]]](
    Child(_, _), Parent(_, _), RootParent(_, _), Created(_, _), Creator(_, _), Contributor(_, _),
    Member(_, _), To(_, _), From(_, _)
  )

  private def hasClass(key: String): Future[Boolean] = {
    // for stuff like empty titles
    if (key.isEmpty) return Future(false)

    val p = Promise[Boolean]()
    gunCalls.get(key, (d: js.Object, k: String) ⇒ {
      if (!js.isUndefined(d)) {
        console.trace(s"Has class gun call got response for $key ${JSON.stringify(d)} $k")
      } else console.trace(s"Has class did not find the requested id $key")
      if (!js.isUndefined(d)) p.success(true) else p.success(false)
    })
    p.future
  }

  def read(d: js.Object, key: String): Future[Option[DataHub[_]]] = {
    val con = d.asInstanceOf[JsDataHub]
    // Id is a special case, since it's value points to an octopus, but it's really a leaf connection
    console.trace(s"ConnectionReader ${con.`class`} ${con.value} $key")
    if (con.`class` == "Id") return Future {Option {Id(con.value)}}

    hasClass(con.value) flatMap { hc ⇒
      console.trace(s"Has class $hc ${con.`class`} ${con.value} $key")
      if (hc) {
        DbReader.read(con.value) map { optO ⇒
          if (optO.isEmpty) throw new Exception(s"Could not read an octopus from database with id ${con.value}")
          optO flatMap { o ⇒
            val res = octopusReaders flatMap { f ⇒ f(con.`class`, o) } headOption;
//            res foreach {_.setOrder(con.order)}
            res
          }
        }
      } else {
        Future {
          val res = leafReaders flatMap { f ⇒ f(con.`class`, con.value) } headOption;
//          res foreach {_.setOrder(con.order)}
          res
        }
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
  private lazy val allCons = hub.connections.map(_.map(_.id))
  lazy val dbDoc = data.getWriterModule(hub).dbDoc // get should never trip

  override def onConnectionsRequest(): Unit = synchronized {
    if (!instantiated) {
      instantiated = true
      console.println(s"Reader got request to load ${hub.getClass} with ${hub.sc.all}")
      dbDoc.exists foreach {ex ⇒ if (ex) load()}
    }
  }

  private def load() = Future {
    console.println(s"Reader loading ${hub} ${hub.id}")

    dbDoc.getData map { data ⇒
      println(s"LOADED ${JSON.stringify(data)}")
    }

//    Future {
//      gunCalls.getConnections(hub.id, (d) ⇒ {
//        console.trace(s"Gun raw connections to read in ${hub} ${hub.id} ${connectionsLeftToLoad}")
//        console.trace(s"${JSON.stringify(d)}")
//        val l = js.Object.keys(d).length
//        connectionsLeftToLoad() = l + connectionsLeftToLoad.now
//        console.trace(s"Gun raw connections length $l ${connectionsLeftToLoad.now}")
//      })
//    }

//    gunCalls.mapConnections(hub.id, (d, k) ⇒ Future {
//      // fixme this will bug out if we are re-using connections
//      val cachedCon = Cache.getConnection(k)
//      if (cachedCon.nonEmpty) {
//        if (!allCons.now.contains(k)) {
//          hub.addConnection(cachedCon.get) foreach { _ ⇒
//            connectionsLeftToLoad() = connectionsLeftToLoad.now - 1
//          }
//        } else connectionsLeftToLoad() = connectionsLeftToLoad.now - 1
//        console.trace(s"Skipping loading connection $k")
//      } else {
//        ConnectionReader.read(d, k) map { optCon ⇒ {
//          console.println(s"Gun read connection of ${hub} $k | ${optCon}")
//          if (optCon.isEmpty) {
//            console.error(s"Reader could not parse connection ${JSON.stringify(d)}")
//            throw new Exception("Gun reader could not parse a connection.")
//          }
//
//          optCon foreach { c ⇒
//            Cache.put(c)
//            val vc = Cache.getConnection(c.id).get // should never fail
//            vc.tmpMarker = GunMarker
//            hub.addConnection(vc) foreach { _ ⇒
//              connectionsLeftToLoad() = connectionsLeftToLoad.now - 1
//            }
//          }
//        }
//        }
//      }
//    })
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
