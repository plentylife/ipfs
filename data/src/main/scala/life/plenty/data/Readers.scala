package life.plenty.data

import life.plenty.model.actions.ActionOnAddToModuleStack
import life.plenty.model.connection._
import life.plenty.model.octopi.GreatQuestions._
import life.plenty.model.octopi._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.language.postfixOps
import scala.scalajs.js

object GunMarker extends TmpMarker

object OctopusReader {
  def ci(className: String, inst: ⇒ Octopus) = {
    (cn: String) ⇒ if (className == cn) Option(inst) else None
  }
  private val availableClasses = Stream[String ⇒ Option[Octopus]](
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
  )

  def read(id: String): Future[Option[Octopus]] = {
    // from cache
    val fromCache = Cache.get(id)
    if (fromCache.nonEmpty) return Future(fromCache)

    val gun = Main.gun.get(id)

    val className = Promise[String]()
    gun.get("class").`val`((d, k) ⇒ {
      // fixme throws an error if id is not present in db
      className.success(d.toLocaleString())
    })

    className.future.map(cs ⇒ {
      println(s"constructing $cs --")
      val r = availableClasses.flatMap(f ⇒ {
        try {
          val o = f(cs)
          o foreach { o ⇒
            o.addConnection(Id(id))
            Cache.put(o)
            o.addModule(new OctopusGunReaderModule(o, gun))
          }
          o
        } catch {
          case e: Throwable ⇒ println(e); e.printStackTrace(); None
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
    Title(_, _), Body(_, _), Amount(_, _), Id(_, _)
  )

  private val octopusReaders = Stream[(String, Octopus) ⇒ Option[Connection[_]]](
    Child(_, _), Parent(_, _)
  )

  private def hasClass(key: String): Future[Boolean] = {
    val p = Promise[Boolean]()
    Main.gun.get(key).`val`((d, k) ⇒ {
      if (!js.isUndefined(d)) {
        //        println(JSON.stringify(d))
        //        println(s"key `$key` has a class property")
        p.success(true)
      } else {
        //        println(s"key `$key` does not")
        p.success(false)
      }
    })
    p.future
  }

  def read(d: js.Object, key: String): Future[Option[Connection[_]]] = {
    //    println(JSON.stringify(d))

    val con = d.asInstanceOf[JsConnection]
    hasClass(con.value) flatMap { hc ⇒
      if (hc) {
        //        println("processing connection value with class")
        OctopusReader.read(con.value) map { optO ⇒
          println("Read octopus", optO)

          if (optO.isEmpty) throw new Exception(s"Could not read an octopus from database with id ${con.value}")
          optO flatMap { o ⇒
            octopusReaders flatMap { f ⇒ f(con.`class`, o) } headOption;
          }
        }
      } else {
        Future(
          leafReaders flatMap { f ⇒ f(con.`class`, con.value) } headOption
        )
      }
    }
  }
}

class OctopusGunReaderModule(override val withinOctopus: Octopus, gun: Gun) extends
  ActionOnAddToModuleStack[Octopus] {
  var loaded = false

  override def onAddToStack(): Unit = {
    println(s"Gun Reader in ${withinOctopus.getClass} with ${withinOctopus.connections}")
    //    gun.`val`((d, k) ⇒ {
    //      println(s"Gun Reader ${withinOctopus.id}")
    //      println(JSON.stringify(d))
    //    })
    gun.get("connections").map().`val`((d, k) ⇒ {
      //      println(s"TRYING loaded connection of ${withinOctopus.getClass} $k", JSON.stringify(d))
      ConnectionReader.read(d, k) map { optCon ⇒ {
        //        println(s"loaded connection of ${withinOctopus.getClass} $k", optCon, JSON.stringify(d))
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
