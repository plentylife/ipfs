package life.plenty.data

import life.plenty.model.actions.ActionOnAddToModuleStack
import life.plenty.model.connection.Connection
import life.plenty.model.octopi.{BasicSpace, Octopus}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.JSON

object OctopusReader {
  private val availableClasses = Stream[String ⇒ Option[Octopus]](
    BasicSpace(_)
  )

  def read(id: String): Future[Option[Octopus]] = {
    val gun = Main.gun.get(id)
    gun.`val`((d, k) ⇒ {
      println("reading")
      println(JSON.stringify(d))
    })

    val className = Promise[String]()
    gun.get("class").`val`((d, k) ⇒ {
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
  def read(d: js.Object): Connection[_] = {
    println("reading connection object")
    println(JSON.stringify(d))


    null
  }
}

class OctopusGunReaderModule(override val withinOctopus: Octopus, gun: Gun) extends
  ActionOnAddToModuleStack[Octopus] {
  override def onAddToStack(): Unit = {
    println("gun reader in initialize of ", withinOctopus, withinOctopus.connections)
    gun.get("connections").map().`val`((d, k) ⇒ {
      println("loading con in", withinOctopus, JSON.stringify(d), k)
    })
  }
}
