package life.plenty.data

import life.plenty.data.Main.gun
import life.plenty.model.actions.ActionAfterGraphTransform
import life.plenty.model.connection.Connection
import life.plenty.model.octopi.Octopus

import scala.scalajs.js

object OctopusWriter {
  def write(o: Octopus): Unit = {
    println(s"writing octopus ${o} ${o.connections}")
    // fixme there should be a check that the class does not already exist
    val go = gun.get(o.id)
    go.put(js.Dynamic.literal(
      "class" → o.getClass.getSimpleName
    ), null)
    //    o.getTopModule({ case m: ConstructorWriterModule[_] ⇒ m }).foreach(_.write(go))

    writeConnections(o.allConnections, go)
  }

  def writeConnections(connections: Iterable[Connection[_]], go: Gun): Unit = {
    val gcons = go.get("connections")
    for (c ← connections) {
      val conGun = ConnectionWriter.write(c)
      //      println("writing connection", c, c.id)
      gcons.set(conGun, null)
    }
  }

  def writeSingleConnection(connection: Connection[_], go: Gun): Unit = {
    println("writing single connection")
    val gcons = go.get("connections")
    val conGun = ConnectionWriter.write(connection)
    gcons.set(conGun, null)
  }
}

object ConnectionWriter {
  def write(c: Connection[_]): Gun = {
    val gc = Main.gun.get(c.id)
    gc.`val`((d, k) ⇒ {
      if (js.isUndefined(d)) {
        println(s"writing connection ${c} ${c.id}")
        val v = getValue(c)
        c.value match {
          case o: Octopus ⇒ OctopusWriter.write(o)
          case _ ⇒
        }
        val obj = js.Dynamic.literal(
          "class" → c.getClass.getSimpleName,
          "value" → v
        )
        gc.put(obj)
      } else {
        println(s"skipped writing connection ${c} ${c.id}")
      }
    })
  }

  private def getValue(c: Connection[_]) = {
    c.value match {
      case o: Octopus ⇒ o.id
      case other ⇒ other.toString()
    }
  }
}

class GunWriterModule(override val withinOctopus: Octopus) extends ActionAfterGraphTransform {
  private lazy val gun = Main.gun.get(withinOctopus.id)

  //  private def readerLoaded = withinOctopus.getTopModule({ case r: OctopusGunReaderModule ⇒ r }) match {
  //    case Some(r: OctopusGunReaderModule) ⇒ r.loaded
  //    case _ ⇒ false
  //  }

  override def onConnectionAdd(connection: Connection[_]): Either[Exception, Unit] = {
    println(s"gunwirtermodue ${withinOctopus} ${connection} ${connection.tmpMarker}")
    println(withinOctopus.modules)
    //    println(s"id ${withinOctopus.idProperty.init} ${withinOctopus.idProperty.getSafe}")
    //    if (connection.tmpMarker != "gun" && readerLoaded) {
    if (connection.tmpMarker != "gun") {
      OctopusWriter.writeSingleConnection(connection, gun)
    }
    Right()
  }

  override def onConnectionRemove(connection: Connection[_]): Either[Exception, Unit] = ???
}