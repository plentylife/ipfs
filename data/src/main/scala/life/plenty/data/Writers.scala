package life.plenty.data

import life.plenty.data.Main.gun
import life.plenty.model.actions.ActionAfterGraphTransform
import life.plenty.model.connection.DataHub
import life.plenty.model.octopi.definition.{AtInstantiation, Module, Hub}
import rx.Ctx

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.JSON

object OctopusWriter {
  def write(o: Hub): Future[Gun] = {
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

      val info = js.Dynamic.literal("class" → o.getClass.getSimpleName, "connections" → null)
      o match {
        case c: DataHub[_] ⇒ info.updateDynamic("value")(stringifyData(c))
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

  private def stringifyData(c: DataHub[_]): String = {
    c.value match {
      case o: Hub ⇒ o.id
      case other ⇒ other.toString()
    }
  }

  def writeConnections(connections: Iterable[DataHub[_]], go: Gun): Unit = {
    if (connections.isEmpty) return

    val gHubs = for (c ← connections) yield OctopusWriter.write(c)
    val singleF = Future.sequence(gHubs)
    singleF foreach {gunsIter ⇒
      val guns = gunsIter.toIndexedSeq
      val s = guns.size
      def recSwE(last: Int): Unit = {
        val i = last + 1
        // acks can happen several times
        var exec = false
        if (!exec && i < s) {
          exec = true
          setWithError(go, guns(i), () ⇒ recSwE(i))
        }
      }
      setWithError(go, guns.head, () ⇒ recSwE(0))
    }
  }

  def writeSingleConnection(connection: DataHub[_], go: Gun): Unit = {
    console.println(s"OctopusWriter single connection ${connection} ${connection.id}")
    OctopusWriter.write(connection) foreach {cgun ⇒
      setWithError(go, cgun)
    }
  }

  private def connectionsGun(ogun: Gun): Gun = ogun.get("connections")

  /**
    * @param hubgun Gun instance pointing to the hub */
  private def setWithError(hubgun: Gun, cgun: Gun, onAck: () ⇒ Unit = () ⇒ Unit) = {
    println(s"set with error, ${hubgun.`_`.soul} -> ${cgun.`_`.soul}")
    connectionsGun(hubgun).set(cgun, (d) ⇒ {
      console.println(s"OctopusWriter in `set` for connection (soul ${cgun.`_`.soul})" +
        s"${JSON.stringify(d)}")
      onAck()
      val ack = d.asInstanceOf[Ack]
      if (!js.isUndefined(ack.err) && ack.err != null) {
        // todo. improve error
        console.error(s"E: write of connection resulted in error ${ack.err}")
      }
    })
  }
}

class GunWriterModule(override val withinOctopus: Hub) extends ActionAfterGraphTransform {
  private lazy val gun = Main.gun.get(withinOctopus.id)

  override def onConnectionAdd(connection: DataHub[_]): Either[Exception, Unit] = {
    if (connection.tmpMarker != GunMarker && connection.tmpMarker != AtInstantiation) {
      Future {
          console.println(s"Gun Writer onConAdd ${withinOctopus} [${withinOctopus.id}] ${connection} ")
          OctopusWriter.writeSingleConnection(connection, gun)
      }
    }
    Right()
  }

  override def onConnectionRemove(connection: DataHub[_]): Either[Exception, Unit] = ???
}

class InstantiationGunWriterModule(override val withinOctopus: Hub) extends Module[Hub] {
  implicit val ctx: Ctx.Owner = Ctx.Owner.safe()

  withinOctopus.onNew {
    Future {
        console.println(s"Instantiation Gun Writer ${withinOctopus} ${withinOctopus.id}")
        OctopusWriter.write(withinOctopus)
    }
  }
}