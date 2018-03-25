package life.plenty.data

import life.plenty.model.connection.DataHub
import life.plenty.model.hub.definition.Hub
import rx.{Ctx, Var}

import scala.collection.mutable

/* todo This should be moved to the model */
object HubCache {
  private implicit val ctx = Ctx.Owner.safe()

  val hubCache = mutable.Map[String, Hub]()
  val dataHubCache = mutable.Map[String, DataHub[_]]()
//  val docCache = mutable.Map[String, DocWrapper]()
  val lastAddedRx: Var[Hub] = Var {null}

  /** @return the existing hub, or the new hub*/
  def put(hub: Hub): Hub = synchronized {
    val existing = getHub(hub.id)
    if (existing.isEmpty) {
      hubCache.put(hub.id, hub)
      if (lastAddedRx.now != hub) lastAddedRx() = hub
      hub
    } else {
      existing.get
    }
  }

  /** @return the existing hub, or the new hub*/
  def put(id: String, hub: DataHub[_]): DataHub[_] = synchronized {
    val existing = dataHubCache.get(id)
    if (existing.isEmpty) {
      dataHubCache.put(id, hub)
      if (lastAddedRx.now != hub) lastAddedRx() = hub
      hub
    } else {
      existing.get
    }
  }

  def getDataHub(id: String) = synchronized {dataHubCache.get(id)}

  def getHub(id: String): Option[Hub] = synchronized {
    hubCache.get(id)
  }

//  def getConnection(id: String): Option[DataHub[_]] = getOctopus(id).map(_.asInstanceOf[DataHub[_]]) // shouldn't fail
}
