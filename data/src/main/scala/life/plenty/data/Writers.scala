package life.plenty.data

import life.plenty.model.actions.ActionAfterGraphTransform
import life.plenty.model.connection.DataHub
import life.plenty.model.octopi.definition.{AtInstantiation, Hub, Module}
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
      return Future {gunCalls.get(o.id, (d, k) ⇒ Unit)} // todo. this can be significantly improved if made optional
    } else {
      // fixme this is danegerous because it does not check for success of the write
      Cache.put(o)
      o.tmpMarker = GunMarker
    }

    forceWrite(o)
  }

  private[data] def forceWrite(o: Hub, hubClass: Option[String] = None) = Future {
    val hc: String = hubClass.getOrElse(o.getClass.getSimpleName)
    val info = js.Dynamic.literal("class" → hc)
    o match {
      case c: DataHub[_] ⇒
        info.updateDynamic("value")(stringifyData(c))
      case _ ⇒
    }

    var firstAck = true
    val holderGun = gunCalls.put(o.id, info, (d) ⇒ {
      console.println(s"OctopusWriter write of ${o.id} resulted in ${JSON.stringify(d)}")
      val ack = d.asInstanceOf[Ack]
      if (!js.isUndefined(ack.err) && ack.err != null) {
        console.error(s"E: OctopusWriter write of ${o} ${o.id} resulted in error ${ack.err}")
      }
      if (firstAck) {
        firstAck = false
      }
    })
    writeConnections(o.id, o.sc.all)
    holderGun
  }

  private def stringifyData(c: DataHub[_]): String = {
    c.value match {
      case o: Hub ⇒ o.id
      case other ⇒ other.toString()
    }
  }

  def writeConnections(holderId: String, connections: Iterable[DataHub[_]]): Unit = {
    if (connections.isEmpty) return

    val gHubs = for (c ← connections) yield OctopusWriter.write(c)
    Future.sequence(gHubs).foreach { ghs ⇒
      console.trace(s"writing into $holderId cons ${connections}")
      setWithError(holderId, ghs.toList)
    }

  }

  // todo. seems like the members not saving is a gun issue all over again
  @deprecated("can't use this because of gun issues; save all")
  def writeSingleConnection(holderId: String, connection: DataHub[_]): Unit = {
    OctopusWriter.write(connection) foreach { cgun ⇒
      console.println(s"OctopusWriter single connection ${connection} ${connection.id}")
      setWithError(holderId, List(cgun))
    }
  }

  private def setWithError(holderId: String, conGun: List[Gun]) = {
    val cons: js.Array[Gun] = js.Array.apply(conGun: _*)
    gunCalls.set(holderId, cons, (d) ⇒ {
      console.trace(s"OctopusWriter in `set` of ${JSON.stringify(d)}")
      val ack = d.asInstanceOf[Ack]
      if (!js.isUndefined(ack.err) && ack.err != null) {
        // todo. improve error
        console.error(s"E: write of connection resulted in error ${ack.err}")
      }
    })
  }
}

class GunWriterModule(override val withinOctopus: Hub) extends ActionAfterGraphTransform {
  private lazy val _gun: Future[Gun] = Future {gunCalls.get(withinOctopus.id, (d, k) ⇒ Unit)}
  private lazy val instModule = withinOctopus.getTopModule({ case m: InstantiationGunWriterModule ⇒ m })

  def gun = instModule.flatMap(m ⇒ m.gun).getOrElse(_gun)

  override def onConnectionAdd(connection: DataHub[_]): Future[Unit] = {
    if (connection.tmpMarker != GunMarker && connection.tmpMarker != AtInstantiation) {
      console.println(s"Gun Writer onConAdd ${withinOctopus} [${withinOctopus.id}] ${connection} ")
//      gun foreach { g ⇒ OctopusWriter.writeSingleConnection(withinOctopus.id, connection) }
      // todo. have to save all because of gun
      gun foreach { g ⇒ OctopusWriter.writeConnections(withinOctopus.id, withinOctopus.sc.all) }
    }
    Future {Right()}
  }
}

class InstantiationGunWriterModule(override val withinOctopus: Hub) extends Module[Hub] {
  implicit val ctx: Ctx.Owner = Ctx.Owner.safe()

  var gun: Option[Future[Gun]] = None

  withinOctopus.onNew {
    console.println(s"Instantiation Gun Writer ${withinOctopus} ${withinOctopus.id}")
    gun = Option(OctopusWriter.write(withinOctopus))
  }
}

class InstantiationSecureUserWriterModule(override val withinOctopus: Hub) extends Module[Hub] {
  implicit val ctx: Ctx.Owner = Ctx.Owner.safe()

  var gun: Option[Future[Gun]] = None

  withinOctopus.onNew {
    console.println(s"Instantiation Gun Writer forcing ${withinOctopus} ${withinOctopus.id}")
    gun = Option(OctopusWriter.forceWrite(withinOctopus, Option("BasicUser")))
  }
}