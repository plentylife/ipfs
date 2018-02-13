package life.plenty.data

import life.plenty.data.Main.gun
import life.plenty.model.actions.ActionAfterGraphTransform
import life.plenty.model.connection.{AtInstantiation, Connection}
import life.plenty.model.octopi.definition.{Module, Octopus}
import rx.Ctx

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.JSON

object OctopusWriter {
  def write(o: Octopus): Future[Gun] = {
    console.println(s"OctopusWriter octopus ${o} ${o.id} ${o.sc.all}")
    if (Cache.getOctopus(o.id).nonEmpty) {
      console.println(s"OctopusWriter skipping octopus ${o} since it is in cache")
      return Future {Main.gun.get(o.id)}
    } else {
      // fixme this is danegerous because it does not check for success of the write
      Cache.put(o)
    }

    Future {
      val go = gun.get(o.id)

      val info = js.Dynamic.literal("class" → o.getClass.getSimpleName)
      o match {
        case c: Connection[_] ⇒ info.updateDynamic("data")(stringifyData(c))
        case _ ⇒
      }

      go.put(info, (d) ⇒ {
        console.println(s"OctopusWriter write of ${o.id} resulted in ${JSON.stringify(d)}")
        val ack = d.asInstanceOf[Ack]
        if (!js.isUndefined(ack.err) && ack.err != null) {
          console.error(s"E: OctopusWriter write of ${o} ${o.id} resulted in error ${ack.err}")
        }
      })
      writeConnections(o.sc.all, go)
      go
    }
  }

  private def stringifyData(c: Connection[_]): String = {
    c.value match {
      case o: Octopus ⇒ o.id
      case other ⇒ other.toString()
    }
  }

  def writeConnections(connections: Iterable[Connection[_]], go: Gun): Unit = {
    val gcons = go.get("connections")
    for (c ← connections) {
      OctopusWriter.write(c) foreach { conGun ⇒
        gcons.set(conGun, null)
      }
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
    if (c.tmpMarker != GunMarker) {
      console.println(s"ConnectionWriter connection ${c} ${c.id}")

      // making sure that we aren't writing it again
      Cache.put(c)
      c.tmpMarker = GunMarker
      // write
    }
    gc
  }
}

class GunWriterModule(override val withinOctopus: Octopus) extends ActionAfterGraphTransform {
  private lazy val gun = Main.gun.get(withinOctopus.id)

  override def onConnectionAdd(connection: Connection[_]): Either[Exception, Unit] = {
    //    console.println(s"Gun Writer ${withinOctopus.id} ${connection} marker: ${connection.tmpMarker}")
    //      withinOctopus.isNew &&
    if (connection.tmpMarker != GunMarker && connection.tmpMarker != AtInstantiation) {
      Future {
          console.println(s"Gun Writer onConAdd ${withinOctopus} [${withinOctopus.id}] ${connection} ")
          OctopusWriter.writeSingleConnection(connection, gun)
      }
    }
    Right()
  }

  override def onConnectionRemove(connection: Connection[_]): Either[Exception, Unit] = ???
}

class InstantiationGunWriterModule(override val withinOctopus: Octopus) extends Module[Octopus] {
  implicit val ctx: Ctx.Owner = Ctx.Owner.safe()

  withinOctopus.onNew {
    Future {
        console.println(s"Instantiation Gun Writer ${withinOctopus} ${withinOctopus.id}")
        OctopusWriter.write(withinOctopus)
    }
  }
}