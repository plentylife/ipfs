package life.plenty.data

import life.plenty.model.actions.ActionOnInitialize
import life.plenty.model.connection.Connection
import life.plenty.model.octopi.Octopus

import scala.concurrent.Promise
import scala.scalajs.js
import scala.scalajs.js.JSON

object OctopusReader {
  def read(id: String): Octopus = {
    val gun = Main.gun.get(id)
    gun.get("connections").`val`((d, k) ⇒ {
      println("all cons as one")
      println(JSON.stringify(d))
    })

    val promise = Promise[Octopus]



    //
    //    gun.get("connections").map().`val`((d, k) ⇒ {
    //      println("loading con", JSON.stringify(d), k)
    //      val c = ConnectionReader.read(d)
    //      o.addConnection(c)
    //    })
    //
    //    p.future
    ???
  }
}


object ConnectionReader {
  def read(d: js.Object): Connection[_] = {
    null
  }
}

class OctopusGunReaderModule(override val withinOctopus: Octopus) extends ActionOnInitialize[Octopus] {
  override def onInitialize(): Unit = {
    Main.gun.get(withinOctopus.id).map().`val`((d, k) ⇒ {
      println("loading con in", withinOctopus, JSON.stringify(d), k)
    })
  }
}
