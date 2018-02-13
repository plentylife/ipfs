package life.plenty.data

import life.plenty.model.connection.DataHub
import life.plenty.model.octopi.definition.Hub
import rx.{Ctx, Var}

import scala.collection.mutable

object Cache {
  private implicit val ctx = Ctx.Owner.safe()

  val octopusCache = mutable.Map[String, Hub]()
  val connectionCache = mutable.Map[String, DataHub[_]]()

  val lastAddedRx: Var[Hub] = Var {null}

  def put(octopus: Hub): Unit = synchronized {
    val existing = getOctopus(octopus.id)
    if (existing.isEmpty) {
      octopusCache.put(octopus.id, octopus)
      if (lastAddedRx.now != octopus) lastAddedRx() = octopus
    }
  }

  def getOctopus(id: String): Option[Hub] = synchronized {
    octopusCache.get(id)
  }

  def getConnection(id: String): Option[DataHub[_]] = getOctopus(id).map(_.asInstanceOf[DataHub[_]]) // shouldn't fail
}
