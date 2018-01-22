package life.plenty.data

import scala.scalajs.js.JSON

object OctopusReader {
  def read(id: String): Unit = read(Main.gun.get(id))

  def read(gun: Gun): Unit = {
    gun.get("connections").map().`val`((d, k) ⇒ {
      println("loading con", JSON.stringify(d), k)
    })
  }
}

//class OctopusReaderModule extends ActionOnInitialize[] {
