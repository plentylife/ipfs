package life.plenty.data

import life.plenty.model.actions.ActionAfterGraphTransform
import life.plenty.model.connection.DataHub
import life.plenty.model.octopi.definition.{AtInstantiation, Hub, Module}
import life.plenty.model.security.SecureUser
import rx.Ctx

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.JSON

object DbWriter {
  def write(o: Hub, doc: AsyncShareDoc): Unit = {
    console.println(s"OctopusWriter octopus ${o} ${o.id} ${o.sc.all}")
    if (Cache.getOctopus(o.id).nonEmpty) {
      console.println(s"OctopusWriter skipping octopus ${o} since it is in cache")
      return Unit
    } else {
      // fixme this is danegerous because it does not check for success of the write
      Cache.put(o)
      o.tmpMarker = GunMarker
    }

    forceWrite(o, doc)
  }

  private[data] def forceWrite(o: Hub, doc: AsyncShareDoc, hubClass: Option[String] = None): Future[Gun] = Future {
    doc.setInitial {
      val hc: String = hubClass.getOrElse(o.getClass.getSimpleName)
      val info = js.Dynamic.literal("class" → hc, "connections" → js.Array(
        o.sc.all.map(_.id)
      ))
      o match {
        case c: DataHub[_] ⇒
          info.updateDynamic("value")(stringifyData(c))
        case _ ⇒
      }
      info
    }

    null
  }

  /** is not safe, but should never fail */
  def getDoc(h: Hub): AsyncShareDoc = h.getTopModule({case m: DbWriterModule ⇒ m}).get.dbDoc

  private def stringifyData(c: DataHub[_]): String = {
    c.value match {
      case o: Hub ⇒ o.id
      case other ⇒ other.toString()
    }
  }

  def writeSingleConnection(holderDoc: AsyncShareDoc, connection: DataHub[_]): Unit = {
    DbWriter.write(connection)
  }

}

class DbWriterModule(override val hub: Hub) extends ActionAfterGraphTransform {
  val dbDoc = new AsyncShareDoc(hub.id, true)

  hub.onNew(onNew)

  protected def onNew = {
    console.println(s"Instantiation Gun Writer ${hub} ${hub.id}")
    DbWriter.write(hub)
  }

  override def onConnectionAdd(connection: DataHub[_]): Future[Unit] = {
    if (connection.tmpMarker != GunMarker && connection.tmpMarker != AtInstantiation) {
      console.println(s"Gun Writer onConAdd ${hub} [${hub.id}] ${connection} ")
//      gun foreach { g ⇒ OctopusWriter.writeSingleConnection(withinOctopus.id, connection) }
      // todo. have to save all because of gun
//      gun foreach { g ⇒ OctopusWriter.writeConnections(hub.id, hub.sc.all) }
    }
    Future {Right()}
  }
}

class SecureUserDbWriterModule(override val hub: SecureUser) extends DbWriterModule(hub) {
  override protected def onNew = {
    console.println(s"Instantiation Gun Writer forcing ${hub} ${hub.id}")
    DbWriter.forceWrite(hub, dbDoc, Option("BasicUser"))
  }
}