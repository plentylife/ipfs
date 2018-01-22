package life.plenty.data

import life.plenty.data.Main.gun
import life.plenty.model.connection.Connection
import life.plenty.model.octopi.Octopus

import scala.scalajs.js

object OctopusWriter {
  def write(o: Octopus): Unit = {
    val go = gun.get(o.id).put(js.Dynamic.literal(
      "class" → o.getClass.getSimpleName
    ), null)

    val gcons = go.get("connections")
    for (c ← o.allConnections) {
      gcons.set(ConnectionWriter.write(c), null)
    }
  }
}

object ConnectionWriter {
  def write(c: Connection[_]): Gun = {
    val v = getValue(c)
    val obj = js.Dynamic.literal(
      "class" → c.getClass.getSimpleName,
      "value" → v
    )
    // fixme. make this a hash
    Main.gun.get(c.id).put(obj)
  }

  def getValue(c: Connection[_]) = {
    c.value match {
      case o: Octopus ⇒ o.id
      case other ⇒ other.toString()
    }
  }
}