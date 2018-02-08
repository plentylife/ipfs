package life.plenty.data

import life.plenty.data
import life.plenty.model.actions.ActionOnConnectionsRequest
import life.plenty.model.connection._
import life.plenty.model.octopi.GreatQuestions._
import life.plenty.model.octopi._
import rx.Var

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.JSON

object GunMarker extends TmpMarker

object OctopusReader {
  def ci(className: String, inst: ⇒ Octopus) = {
    (cn: String) ⇒ if (className == cn) Option(inst) else None
  }

  private val availableClasses = Stream[String ⇒ Option[Octopus]](
    ci("BasicUser", new BasicUser()),
    ci("BasicSpace", new BasicSpace()),
    ci("BasicQuestion", new BasicQuestion()),
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

  def read(id: String): Future[Option[Octopus]] = {
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
      r
    })
  }
}


object ConnectionReader {

  @js.native
  trait JsConnection extends js.Object {
    val `class`: String
    val value: String
  }

  private val leafReaders = Stream[(String, String) ⇒ Option[Connection[_]]](
    Title(_, _), Body(_, _), Amount(_, _), Id(_, _), Name(_, _), CreationTime(_, _), Marker(_, _), Removed(_, _)
  )

  private val octopusReaders = Stream[(String, Octopus) ⇒ Option[Connection[_]]](
    Child(_, _), Parent(_, _), Created(_, _), Creator(_, _), Contributor(_, _), Member(_, _), To(_, _), From(_, _)
  )

  private def hasClass(key: String): Future[Boolean] = {
    // for stuff like empty titles
    if (key.isEmpty) return Future(false)

    val p = Promise[Boolean]()
    Main.gun.get(key).`val`((d, k) ⇒ {
      if (!js.isUndefined(d)) {
        //        console.println(JSON.stringify(d))
        //        console.println(s"key `$key` has a class property")
        p.success(true)
      } else {
        //        console.println(s"key `$key` does not")
        p.success(false)
      }
    })
    p.future
  }

  def read(d: js.Object, key: String): Future[Option[Connection[_]]] = {
    val con = d.asInstanceOf[JsConnection]
    // Id is a special case, since it's value points to an octopus, but it's really a leaf connection
    if (con.`class` == "Id") return Future {Option {Id(con.value)}}
    if (con.`class` == "Removed") return Future {Option {Removed(con.value)}}
    console.trace(s"ConnectionReader ${con.`class`} ${con.value} $key")

    hasClass(con.value) flatMap { hc ⇒
      if (hc) {
        OctopusReader.read(con.value) map { optO ⇒
          if (optO.isEmpty) throw new Exception(s"Could not read an octopus from database with id ${con.value}")
          optO flatMap { o ⇒
            octopusReaders flatMap { f ⇒ f(con.`class`, o) } headOption;
          }
        }
      } else {
        Future {
          //          console.trace(s"ConnectionReader leafReader ${con.`class`} ${con.value} $key")
          val res = leafReaders flatMap { f ⇒ f(con.`class`, con.value) } headOption;
          //          console.trace(s"ConnectionReader leafReader ${con.`class`} ${con.value} $key $res")
          res
        }
      }
    }
  }
}

// todo create a module for user that filters out everything but transactions

class OctopusGunReaderModule(override val withinOctopus: Octopus) extends ActionOnConnectionsRequest {
  private implicit val ctx = withinOctopus.ctx

  var instantiated = false
  val connectionsLeftToLoad = Var(-1)
  console.println(s"Gun Reader instantiated in ${withinOctopus.getClass}")

  private lazy val allCons = withinOctopus.rx.allCons.map(_.map(_.id))

  override def onConnectionsRequest(): Unit = synchronized {
    if (!instantiated) {
      instantiated = true
      console.println(s"Gun Reader ${this} onConsReq called in ${withinOctopus.getClass} with ${
        withinOctopus
          .connections
      }")
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
      //      setTimeout(10) {
      if (allCons.now.contains(k)) {
        connectionsLeftToLoad() = connectionsLeftToLoad.now - 1
        console.trace(s"Skipping loading connection $k")
      } else {
        ConnectionReader.read(d, k) map { optCon ⇒ {
          console.println(s"Gun read connection of ${withinOctopus} $k | ${optCon}")
          if (optCon.isEmpty) {
            console.error(s"Reader could not parse connection ${JSON.stringify(d)}")
            throw new Exception("Gun reader could not parse a connection.")
          }

          optCon foreach { c ⇒
            connectionsLeftToLoad() = connectionsLeftToLoad.now - 1
            c.tmpMarker = GunMarker
            withinOctopus.addConnection(c)
          }
        }
        }
      }
      //      }
    })
  }
}

object OctopusGunReaderModule {
  def onFinishLoad(o: Octopus, f: () ⇒ Unit) = {
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
