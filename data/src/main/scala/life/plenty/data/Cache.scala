package life.plenty.data

import life.plenty.model.connection.Connection
import life.plenty.model.octopi.definition.Octopus
import rx.{Ctx, Var}

import scala.collection.mutable

object Cache {
  private implicit val ctx = Ctx.Owner.safe()

  val octopusCache = mutable.Map[String, Octopus]()
  val connectionCache = mutable.Map[String, Connection[_]]()

  val lastAddedRx: Var[Octopus] = Var {null}

  def put(octopus: Octopus): Unit = synchronized {
    val existing = getOctopus(octopus.id)
    if (existing.isEmpty) {
      octopusCache.put(octopus.id, octopus)
      if (lastAddedRx.now != octopus) lastAddedRx() = octopus
    }
  }

  def getOctopus(id: String): Option[Octopus] = synchronized {
    octopusCache.get(id)
  }

  def put(c: Connection[_]): Unit = synchronized {connectionCache.put(c.id, c)}

  def getConnection(id: String): Option[Connection[_]] = connectionCache.get(id)
}
