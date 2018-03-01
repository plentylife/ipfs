package life.plenty.data

import life.plenty.model.actions.ActionAfterGraphTransform
import life.plenty.model.connection.DataHub
import life.plenty.model.octopi.definition.{AtInstantiation, Hub, Module}
import life.plenty.model.security.SecureUser
import rx.Ctx

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.{Any, JSON}

class DbInsertConnectionOp(h: Hub) extends DbInsertOp {
  override val p: js.Array[js.Any] = js.Array("connections", 0)
  override val li: js.Any = h.id
}

object DbWriter {
  def writeInitial(o: Hub, doc: Option[DocWrapper] = None): Future[Unit] = synchronized {
    console.println(s"OctopusWriter octopus ${o} ${o.id} ${o.sc.all}")
    if (Cache.getOctopus(o.id).nonEmpty) {
      console.println(s"OctopusWriter skipping octopus ${o} since it is in cache")
      return Future{}
    } else {
      // fixme this is danegerous because it does not check for success of the write
      Cache.put(o)
      o.tmpMarker = DbMarker
    }

    val dbDoc = doc.getOrElse(getDoc(o))

    forceWriteInitial(o, dbDoc)
  }

  private[data] def forceWriteInitial(o: Hub, doc: DocWrapper, hubClass: Option[String] = None): Future[Unit] =
  synchronized {
    val hc: String = hubClass.getOrElse(o.getClass.getSimpleName)
    val connections = o.sc.all

    //       because datahubs aren't automatically saved by their module (no asNew call)
    // and it needs to happen after the connection has been given to a holder (id depends on it)
    val cwStatus = Future.sequence(connections map { c ⇒ writeInitial(c) })

    // this needs to get processed outside of set initial, otherwise we might end up with duplicates
    val info = js.Dynamic.literal("class" → hc, "connections" → js.Array(
      connections.map(_.id): _*
    ))
    o match {
      case c: DataHub[_] ⇒ fillDataHubInfo(c, info)
      case _ ⇒
    }

    // we have to wait for all connections to be written first, otherwise the receiving end will not find them
    cwStatus flatMap { _ ⇒ doc.setInitial(info)} recover {
      case e: Throwable ⇒ console.error("Failed to save to database on initial write")
        console.error(e)
        throw e
    }
  }

  private def fillDataHubInfo(c: DataHub[_], info: js.Dynamic): Unit = {
    c.value match {
      case o: Hub ⇒
        info.updateDynamic("value")(o.id)
        info.updateDynamic("valueType")("hub")
      case other ⇒
        info.updateDynamic("value")(other.toString)
        info.updateDynamic("valueType")("string")
    }
  }

  def writeSingleConnection(holderDoc: DocWrapper, connection: DataHub[_]): Unit = {
    DbWriter.writeInitial(connection) foreach { _ ⇒ // write the new connection
      // add to holder
      holderDoc.submitOp(new DbInsertConnectionOp(connection))
    }
  }

}

class DbWriterModule(override val hub: Hub) extends ActionAfterGraphTransform {
  private var _dbDoc: DocWrapper = null
  protected[data] def dbDoc = if (_dbDoc != null) _dbDoc else {
    _dbDoc = new DocWrapper(hub.id)
    _dbDoc
  }

  // as a preventitive measure from having too many docs kick aroud
  /** @return the doc that is actively used */
  protected[data] def setDbDoc(doc: DocWrapper): DocWrapper = {
    if (_dbDoc == null) _dbDoc = doc
    _dbDoc
  }

  console.trace(s"DbWriter Module instantiated in ${hub.getClass.getSimpleName} ${this.getClass}")

  hub.onNew(onNew())

  protected def onNew() = {
    console.println(s"Instantiation Gun Writer ${hub} ${hub.id} ${hub.sc.all}")
    DbWriter.writeInitial(hub, Option(dbDoc))
  }

  override def onConnectionAdd(connection: DataHub[_]): Future[Unit] = {
    // todo. should in theory not write any connections that already exist in the database, but it would be best to
    // check
    console.println(s"DbWriter on new connection added (MAYBE) ${hub} [${hub.id}] ${connection} ${connection.id}")
    if (connection.tmpMarker != DbMarker && connection.tmpMarker != AtInstantiation) {
      console.println(s"DbWriter on new connection added ${hub} [${hub.id}] ${connection} ")
      DbWriter.writeSingleConnection(dbDoc, connection)
    }
    Future()
  }
}

class SecureUserDbWriterModule(override val hub: SecureUser) extends DbWriterModule(hub) {
  override protected def onNew = {
    console.println(s"Instantiation Gun Writer forcing ${hub} ${hub.id}")
    DbWriter.forceWriteInitial(hub, dbDoc, Option("BasicUser"))
  }
}