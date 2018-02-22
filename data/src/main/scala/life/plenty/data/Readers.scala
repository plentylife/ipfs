package life.plenty.data

import life.plenty.data
import life.plenty.model.actions.{ActionOnConnectionsRequest, ActionOnFinishDataLoad}
import life.plenty.model.connection._
import life.plenty.model.octopi.GreatQuestions._
import life.plenty.model.octopi._
import life.plenty.model.octopi.definition.{Hub, TmpMarker}
import rx.Var

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.JSON

object GunMarker extends TmpMarker

object OctopusReader {
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

  def read(id: String): Future[Option[Hub]] = {
    data.console.trace(s"Reading hub with id `${id}`")
    // from cache
    val fromCache = Cache.getOctopus(id)
    if (fromCache.nonEmpty) {
      data.console.println(s"Read ${id} from cache")
      return Future(fromCache)
    }

//    val gun = Main.gun.get(id)

    val className = Promise[String]()
    gunCalls.getHubClass(id, (d) ⇒ {
      if (!js.isUndefined(d) && d != null) {
        className.success(d.toLocaleString())
      } else {
        console.error(s"Failed loading on ID `$id`")
        className.failure(new Exception(s"Could not find id $id in the database"))
      }
    })

    // fixme. optimization. not try every loader. stop at first success.
    className.future.flatMap(cs ⇒ {
      console.println(s"Gun is constructing $cs")
      val potentials: Stream[Future[Option[Hub]]] = availableClasses.map(f ⇒ {
        try {
          val o = f(cs)
          val res: Future[Option[Hub]] = o map { o ⇒
            val idCon = Id(id)
            idCon.tmpMarker = GunMarker
            // have to wait on id!!
            o.addConnection(idCon) map {_ ⇒
              Cache.put(o)
              // fixme this is just a quick fix. for not double loading
            } map {_ ⇒ Cache.getOctopus(id)}
          } getOrElse Future {Cache.getOctopus(id)}
          res
        } catch {
          case e: Throwable ⇒ console.error(e); e.printStackTrace(); Future {None}
        }
      })

      Future.sequence(potentials) map { materialized ⇒
        val actualized = materialized.flatten
        if (actualized.isEmpty) console.error(s"Could not find loader with class ${cs}")
        actualized.headOption
      }
    })
  }
}


object ConnectionReader {

  @js.native
  trait JsConnection extends js.Object {
    val `class`: String
    val active: Boolean
    val order: Int
    val value: String
  }

  private val leafReaders = Stream[(String, String) ⇒ Option[DataHub[_]]](
    Title(_, _), Body(_, _), Amount(_, _), Id(_, _), Name(_, _), CreationTime(_, _), Marker(_, _),
    Active(_, _), Inactive(_, _)
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
    val con = d.asInstanceOf[JsConnection]
    // Id is a special case, since it's value points to an octopus, but it's really a leaf connection
    console.trace(s"ConnectionReader ${con.`class`} ${con.value} $key")
    if (con.`class` == "Id") return Future {Option {Id(con.value)}}

    hasClass(con.value) flatMap { hc ⇒
      console.trace(s"Has class $hc ${con.`class`} ${con.value} $key")
      if (hc) {
        OctopusReader.read(con.value) map { optO ⇒
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

class OctopusGunReaderModule(override val withinOctopus: Hub) extends ActionOnConnectionsRequest with
ActionOnFinishDataLoad {
  private implicit val ctx = withinOctopus.ctx

  var instantiated = false
  val connectionsLeftToLoad = Var(-1)
  console.println(s"Gun Reader instantiated in ${withinOctopus.getClass}")

  private lazy val allCons = withinOctopus.connections.map(_.map(_.id))

  override def onConnectionsRequest(): Unit = synchronized {
    if (!instantiated) {
      instantiated = true
      console.println(s"Gun Reader ${this} onConsReq called in ${withinOctopus.getClass} with ${withinOctopus.sc.all}")
      gunCalls.get(withinOctopus.id, (d, k) ⇒ {
        if (!js.isUndefined(d) && d != null) {
          console.trace(s"Gun Reader onConsReq before load() ${withinOctopus.id} ${JSON.stringify(d)}")
          load()
        }
      })
    }
  }

  private def load() = Future {
    console.println(s"Gun reader ${this} setting up in load() of ${withinOctopus} ${withinOctopus.id}")
//    val gc = gun.get("connections")

    Future {
      gunCalls.getConnections(withinOctopus.id, (d) ⇒ {
        console.trace(s"Gun raw connections to read in ${withinOctopus} ${withinOctopus.id} ${connectionsLeftToLoad}")
        console.trace(s"${JSON.stringify(d)}")
        val l = js.Object.keys(d).length
        connectionsLeftToLoad() = l + connectionsLeftToLoad.now
        console.trace(s"Gun raw connections length $l ${connectionsLeftToLoad.now}")
      })
    }

    gunCalls.mapConnections(withinOctopus.id, (d, k) ⇒ Future {
      // fixme this will bug out if we are re-using connections
      val cachedCon = Cache.getConnection(k)
      if (cachedCon.nonEmpty) {
        connectionsLeftToLoad() = connectionsLeftToLoad.now - 1
        if (!allCons.now.contains(k)) withinOctopus.addConnection(cachedCon.get)
        console.trace(s"Skipping loading connection $k")
      } else {
        ConnectionReader.read(d, k) map { optCon ⇒ {
          console.println(s"Gun read connection of ${withinOctopus} $k | ${optCon}")
          if (optCon.isEmpty) {
            console.error(s"Reader could not parse connection ${JSON.stringify(d)}")
            throw new Exception("Gun reader could not parse a connection.")
          }

          optCon foreach { c ⇒
            Cache.put(c)
            val vc = Cache.getConnection(c.id).get // should never fail
            connectionsLeftToLoad() = connectionsLeftToLoad.now - 1
            vc.tmpMarker = GunMarker
            withinOctopus.addConnection(vc)
          }
        }
        }
      }
    })
  }

  override def onFinishLoad(f: () ⇒ Unit): Unit = connectionsLeftToLoad.foreach(count ⇒ if (count == 0) {
    f()
    connectionsLeftToLoad.kill()
  })
}

object OctopusGunReaderModule {
  def onFinishLoad(o: Hub, f: () ⇒ Unit) = {
    implicit val _ctx = o.ctx
    o.onModulesLoad {
      o.getTopModule({ case m: OctopusGunReaderModule ⇒ m }).foreach {
        m ⇒ m.onFinishLoad(f)
      }
    }
  }
}
