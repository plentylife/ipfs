package life.plenty.data

import life.plenty.data.Main.gun
import life.plenty.model.actions.ActionAfterGraphTransform
import life.plenty.model.connection.Connection
import life.plenty.model.octopi.definition.{AtInstantiation, Module, Octopus}
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
      return Future {Main.gun.get(o.id)} // todo. this can be significantly improved if made optional
    } else {
      // fixme this is danegerous because it does not check for success of the write
      Cache.put(o)
      o.tmpMarker = GunMarker
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
    val consgun = connectionsGun(go)
    for (c ← connections) {
      OctopusWriter.write(c) foreach { cgun ⇒
        setWithError(consgun, cgun)
      }
    }
  }

  def writeSingleConnection(connection: Connection[_], go: Gun): Unit = {
    console.println(s"OctopusWriter single connection ${connection} ${connection.id}")
    val consgun = connectionsGun(go)
    OctopusWriter.write(connection) foreach {cgun ⇒
      setWithError(consgun, cgun)
    }
  }

  private def connectionsGun(ogun: Gun): Gun = ogun.get("connections")

  /**
    * @param consgun Gun instance pointing to the `connections` field */
  private def setWithError(consgun: Gun, cgun: Gun) = {
    consgun.set(cgun, (d) ⇒ {
      console.println(s"OctopusWriter in `set` for connection ${JSON.stringify(d)}")
      val ack = d.asInstanceOf[Ack]
      if (!js.isUndefined(ack.err) && ack.err != null) {
        // todo. improve error
        console.error(s"E: write of connection resulted in error ${ack.err}")
      }
    })
  }
}

class GunWriterModule(override val withinOctopus: Octopus) extends ActionAfterGraphTransform {
  private lazy val gun = Main.gun.get(withinOctopus.id)

  override def onConnectionAdd(connection: Connection[_]): Either[Exception, Unit] = {
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