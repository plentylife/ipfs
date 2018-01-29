package life.plenty.data

import life.plenty.model.actions.ActionOnConnectionsRequest
import life.plenty.model.connection._
import life.plenty.model.octopi.GreatQuestions._
import life.plenty.model.octopi._

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
    ci("BasicAnswer", new BasicAnswer()),
    ci("Contribution", new Contribution()),
    ci("Vote", new Vote),
    ci("Transaction", new Transaction),
    ci("VoteAllowance", new VoteAllowance)
  )

  def read(id: String): Future[Option[Octopus]] = {
    // from cache
    val fromCache = Cache.get(id)
    if (fromCache.nonEmpty) {
      //      console.println("Read from cache")
      return Future(fromCache)
    }

    val gun = Main.gun.get(id)

    val className = Promise[String]()
    gun.get("class").`val`((d, k) ⇒ {
      // fixme throws an error if id is not present in db
      try {
        className.success(d.toLocaleString())
      } catch {
        case e: Throwable ⇒ console.error(s"Failed on ID `$id`"); throw e
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
          o
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
    Title(_, _), Body(_, _), Amount(_, _), Id(_, _), Name(_, _), CreationTime(_, _), Marker(_, _)
  )

  private val octopusReaders = Stream[(String, Octopus) ⇒ Option[Connection[_]]](
    Child(_, _), Parent(_, _), Created(_, _), Creator(_, _), Contributor(_, _), Member(_, _), To(_, _), From(_, _)
  )

  private def hasClass(key: String): Future[Boolean] = {
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

    hasClass(con.value) flatMap { hc ⇒
      if (hc) {
        OctopusReader.read(con.value) map { optO ⇒
          //          console.println(s"Read octopus $optO ${optO.map(_.connections).getOrElse(List())}")

          if (optO.isEmpty) throw new Exception(s"Could not read an octopus from database with id ${con.value}")
          optO flatMap { o ⇒
            octopusReaders flatMap { f ⇒ f(con.`class`, o) } headOption;
          }
        }
      } else {
        Future {
          leafReaders flatMap { f ⇒ f(con.`class`, con.value) } headOption
        }
      }
    }
  }
}

class OctopusGunReaderModule(override val withinOctopus: Octopus) extends ActionOnConnectionsRequest {
  var loaded = false
  console.println(s"Gun Reader instantiated in ${withinOctopus.getClass}")

  override def onConnectionsRequest(): Unit = if (!loaded) {
    console.println(s"Gun Reader in ${withinOctopus.getClass} with ${withinOctopus.connections}")
    val gun = Main.gun.get(withinOctopus.id)
    gun.`val`((d, k) ⇒ {
      if (!js.isUndefined(d)) load(gun)
    })
  }

  private def load(gun: Gun) = {
    gun.get("connections").map().`val`((d, k) ⇒ {
      ConnectionReader.read(d, k) map { optCon ⇒ {
        console.println(s"Gun read connection of ${withinOctopus} $k | ${optCon}")
        if (optCon.isEmpty) {
          console.println(JSON.stringify(d))
          throw new Exception("Gun reader could not parse a connection.")
        }

        optCon foreach { c ⇒
          c.tmpMarker = GunMarker
          withinOctopus.addConnection(c)
        }
      }
      }
    })

    loaded = true
  }
}
