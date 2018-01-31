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
    console.println(s"OctopusWriter octopus ${o} ${o.id} ${o.connections}")
    if (Cache.get(o.id).nonEmpty) {
      console.println(s"OctopusWriter skipping octopus ${o} since it is in cache")
      return
    } else {
      // fixme this is danegerous because it does not check for success of the write
      Cache.put(o)
    }

    val go = gun.get(o.id)
    go.put(js.Dynamic.literal(
      "class" → o.getClass.getSimpleName
    ), (d) ⇒ {
      // fixme add error
      console.println(s"OctopusWriter write of ${o.id} resulted in ${JSON.stringify(d)}")
      val ack = d.asInstanceOf[Ack]
      if (!js.isUndefined(ack.err) && ack.err != null) {
        console.error(s"E: OctopusWriter write of ${o} ${o.id} resulted in error ${ack.err}")
      }
    })
    //    o.getTopModule({ case m: ConstructorWriterModule[_] ⇒ m }).foreach(_.write(go))

    writeConnections(o.allConnections, go)
  }

  def writeConnections(connections: Iterable[Connection[_]], go: Gun): Unit = {
    val gcons = go.get("connections")
    for (c ← connections) {
      val conGun = ConnectionWriter.write(c)
      gcons.set(conGun, null)
    }
  }

  def writeSingleConnection(connection: Connection[_], go: Gun): Unit = {
    console.println(s"OctopusWriter single connection ${connection} ${connection.id}")
    val gcons = go.get("connections")
    val conGun = ConnectionWriter.write(connection)
    gcons.set(conGun, (d) ⇒ {
      console.println(s"OctopusWriter done single connection ${JSON.stringify(d)}")
      val ack = d.asInstanceOf[Ack]
      if (!js.isUndefined(ack.err) && ack.err != null) {
        console.error(s"E: write of single connection ${connection} ${connection.id} resulted in error ${ack.err}")
      }
    })
  }
}

object ConnectionWriter {
  def write(c: Connection[_]): Gun = {
    val gc = Main.gun.get(c.id)
    gc.`val`((d, k) ⇒ {
      console.println(s"ConnectionWriter write val is triggered ${c.id}")
      if (js.isUndefined(d)) {
        console.println(s"ConnectionWriter connection ${c} ${c.id}")
        val v = getValue(c)
        c.value match {
          case o: Octopus ⇒ OctopusWriter.write(o)
          case _ ⇒
        }
        val obj = js.Dynamic.literal(
          "class" → c.getClass.getSimpleName,
          "value" → v
        )
        gc.put(obj, (d) ⇒ {
          val ack = d.asInstanceOf[Ack]
          if (!js.isUndefined(ack.err) && ack.err != null) {
            console.error(s"E: ConnectionWriter write of ${c} ${c.id} resulted in error ${ack.err}")
          }
        })
      } else {
        console.println(s"skipped writing connection ${c} ${c.id}")
        // this might means that a connection to a different octopus is getting reused
      }
    })
    gc
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
    //    console.println(s"Gun Writer ${withinOctopus.id} ${connection} marker: ${connection.tmpMarker}")
    //      withinOctopus.isNew &&
    if (connection.tmpMarker != GunMarker && connection.tmpMarker != AtInstantiation) {
      console.println(s"Gun Writer onConAdd ${withinOctopus} [${withinOctopus.id}] ${connection} ")
      OctopusWriter.writeSingleConnection(connection, gun)
    }
    Right()
  }

  override def onConnectionRemove(connection: Connection[_]): Either[Exception, Unit] = ???
}

class InstantiationGunWriterModule(override val withinOctopus: Octopus) extends Module[Octopus] {
  implicit val ctx: Ctx.Owner = Ctx.Owner.safe()

  withinOctopus.onNew {
    console.println(s"Instantiation Gun Writer ${withinOctopus} ${withinOctopus.id}")
    OctopusWriter.write(withinOctopus)
  }
}