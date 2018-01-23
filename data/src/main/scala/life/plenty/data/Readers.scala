package life.plenty.data

import life.plenty.model.actions.ActionOnAddToModuleStack
import life.plenty.model.connection.{Connection, Title}
import life.plenty.model.octopi.{BasicSpace, Octopus}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.JSON

object OctopusReader {
  private val availableClasses = Stream[String ⇒ Option[Octopus]](
    BasicSpace(_)
  )

  def read(id: String): Future[Option[Octopus]] = {
    val gun = Main.gun.get(id)

    val className = Promise[String]()
    gun.get("class").`val`((d, k) ⇒ {
      // fixme throws an error if id is not present
      className.success(d.toLocaleString())
    })

    className.future.map(cs ⇒ {
      println(s"constructing $cs --")
      val r = availableClasses.flatMap(f ⇒ {
        try {
          val o = f(cs)
          o foreach { o ⇒ o.addModule(new OctopusGunReaderModule(o, gun)) }
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
    Title(_, _)
  )

  private def hasClass(key: String): Future[Boolean] = {
    val p = Promise[Boolean]()
    Main.gun.get(key).`val`((d, k) ⇒ {
      if (!js.isUndefined(d)) {
        println(JSON.stringify(d))
        println(s"key `$key` has a class property")
        p.success(true)
      } else {
        println(s"key `$key` does not")
        p.success(false)
      }
    })
    p.future
  }

  def read(d: js.Object, key: String): Future[Option[Connection[_]]] = {
    println("reading connection object")
    println(JSON.stringify(d))

    //    val p = Promise[Option[Connection[_]]]()
    //    Main.gun.get(key).get("value").`val`((d, k) ⇒ {
    val con = d.asInstanceOf[JsConnection]
    println("serialized into", con)
    hasClass(con.value) map { hc ⇒
      if (hc) {
        null
      } else {
        leafReaders flatMap { f ⇒ f(con.`class`, con.value) } headOption;
        //          val res = leafReaders flatMap { f ⇒ f(con.`class`, con.value) } headOption;
        //          p.success(res)
      }
    }
    //    })

    //    p.future
  }
}

class OctopusGunReaderModule(override val withinOctopus: Octopus, gun: Gun) extends
  ActionOnAddToModuleStack[Octopus] {
  override def onAddToStack(): Unit = {
    println("gun reader in initialize of ", withinOctopus, withinOctopus.connections)
    gun.get("connections").map().`val`((d, k) ⇒ {
      ConnectionReader.read(d, k) map { c ⇒ {
        println("loaded connection", c)
      }
      }
    })
  }
}
