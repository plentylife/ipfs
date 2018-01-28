package life.plenty.data

import life.plenty.data.Main.gun
import life.plenty.model.actions.ActionAfterGraphTransform
import life.plenty.model.connection.{AtInstantiation, Connection}
import life.plenty.model.octopi.{Module, Octopus}
import rx.Ctx

import scala.scalajs.js
import scala.scalajs.js.JSON

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
    println(s"writing single connection ${connection}")
    val gcons = go.get("connections")
    val conGun = ConnectionWriter.write(connection)
    gcons.set(conGun, null)
    go.`val`((d, k) ⇒ {
      println(s"done writing single connection")
      println(JSON.stringify(d))
    })
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

  override def onConnectionAdd(connection: Connection[_]): Either[Exception, Unit] = {
    //    println(s"Gun Writer ${withinOctopus.id} ${connection} marker: ${connection.tmpMarker}")
    if (withinOctopus.instantiated.now &&
      connection.tmpMarker != GunMarker && connection.tmpMarker != AtInstantiation) {
      println(s"Gun Writer ${withinOctopus} [${withinOctopus.id}] ${connection} ")
      OctopusWriter.writeSingleConnection(connection, gun)
    }
    Right()
  }

  override def onConnectionRemove(connection: Connection[_]): Either[Exception, Unit] = ???
}

class InstantiationGunWriterModule(override val withinOctopus: Octopus) extends Module[Octopus] {
  implicit val ctx: Ctx.Owner = Ctx.Owner.safe()

  withinOctopus.onInstantiate {
    println(s"Instantiation Gun Writer ${withinOctopus} ${withinOctopus.id}")
    OctopusWriter.write(withinOctopus)
  }
}