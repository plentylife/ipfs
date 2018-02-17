package life.plenty.data

import life.plenty.data
import life.plenty.model.actions.ActionOnConnectionsRequest
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
    // from cache
    val fromCache = Cache.getOctopus(id)
    if (fromCache.nonEmpty) {
      data.console.println(s"Read ${id} from cache")
      return Future(fromCache)
    }

    val gun = Main.gun.get(id)

    val className = Promise[String]()
    gun.get("class").`val`((d, k) ⇒ {
      if (!js.isUndefined(d) && d != null) {
        className.success(d.toLocaleString())
      } else {
        console.error(s"Failed loading on ID `$id`")
        className.failure(new Exception(s"Could not find id $id in the database"))
      }
    })

    className.future.map(cs ⇒ {
      console.println(s"Gun is constructing $cs")
      val r = availableClasses.flatMap(f ⇒ {
        try {
          val o = f(cs)
          o foreach { o ⇒
            val idCon = Id(id)
            idCon.tmpMarker = GunMarker
            o.addConnection(idCon)
            Cache.put(o)
            //            o.addModule(new OctopusGunReaderModule(o, gun))
          }
          // fixme this is just a quick fix. for not double loading
          Cache.getOctopus(id)
          //          o
        } catch {
          case e: Throwable ⇒ console.error(e); e.printStackTrace(); None
        }
      }).headOption
      if(r.isEmpty) console.error(s"Could not find loader with class ${cs}")
      r
    })
  }
}


object ConnectionReader {

  @js.native
  trait JsConnection extends js.Object {
    val `class`: String
    val active: Boolean
    val value: String
  }

  private val leafReaders = Stream[(String, String) ⇒ Option[DataHub[_]]](
    Title(_, _), Body(_, _), Amount(_, _), Id(_, _), Name(_, _), CreationTime(_, _), Marker(_, _)
  )

  private val octopusReaders = Stream[(String, Hub) ⇒ Option[DataHub[_]]](
    Child(_, _), Parent(_, _), Created(_, _), Creator(_, _), Contributor(_, _), Member(_, _), To(_, _), From(_, _)
  )

  private def hasClass(key: String): Future[Boolean] = {
    // for stuff like empty titles
    if (key.isEmpty) return Future(false)

    val p = Promise[Boolean]()
    Main.gun.get(key).`val`((d, k) ⇒ {
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
      if (hc) {
        OctopusReader.read(con.value) map { optO ⇒
          if (optO.isEmpty) throw new Exception(s"Could not read an octopus from database with id ${con.value}")
          optO flatMap { o ⇒
            val res = octopusReaders flatMap { f ⇒ f(con.`class`, o) } headOption;
            res
          }
        }
      } else {
        Future {
          val res = leafReaders flatMap { f ⇒ f(con.`class`, con.value) } headOption;
          res
        }
      }
    }
  }
}

// todo create a module for user that filters out everything but transactions

class OctopusGunReaderModule(override val withinOctopus: Hub) extends ActionOnConnectionsRequest {
  private implicit val ctx = withinOctopus.ctx

  var instantiated = false
  val connectionsLeftToLoad = Var(-1)
  console.println(s"Gun Reader instantiated in ${withinOctopus.getClass}")

  private lazy val allCons = withinOctopus.connections.map(_.map(_.id))

  override def onConnectionsRequest(): Unit = synchronized {
    if (!instantiated) {
      instantiated = true
      console.println(s"Gun Reader ${this} onConsReq called in ${withinOctopus.getClass} with ${withinOctopus.sc.all}")
      val gun = Main.gun.get(withinOctopus.id)
      gun.`val`((d, k) ⇒ {
        if (!js.isUndefined(d) && d != null) {
          console.trace(s"Gun Reader onConsReq before load() ${withinOctopus.id} ${JSON.stringify(d)}")
          load(gun)
        }
      })
    }
  }

  private def load(gun: Gun) = Future {
    console.println(s"Gun reader ${this} setting up in load() of ${withinOctopus} ${withinOctopus.id}")
    val gc = gun.get("connections")

    Future {
      gc.`val`((d, k) ⇒ {
        console.trace(s"Gun raw connections to read in ${withinOctopus} ${withinOctopus.id} ${connectionsLeftToLoad}")
        console.trace(s"${JSON.stringify(d)}")
        val l = js.Object.keys(d).length
        connectionsLeftToLoad() = l + connectionsLeftToLoad.now
        console.trace(s"Gun raw connections length $l ${connectionsLeftToLoad.now}")
      })
    }

    gc.map().`val`((d, k) ⇒ Future {
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
}

object OctopusGunReaderModule {
  def onFinishLoad(o: Hub, f: () ⇒ Unit) = {
    implicit val _ctx = o.ctx
    o.onModulesLoad {
      o.getTopModule({ case m: OctopusGunReaderModule ⇒ m }).foreach {
        m ⇒
          m.connectionsLeftToLoad.foreach(count ⇒ if (count == 0) {
            f()
            m.connectionsLeftToLoad.kill()
          })
      }
    }
  }
}
