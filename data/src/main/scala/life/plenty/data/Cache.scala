package life.plenty.data

import life.plenty.model.octopi.Octopus
import rx.{Ctx, Var}

import scala.collection.mutable

object Cache {
  private implicit val ctx = Ctx.Owner.safe()

  val cache = mutable.Map[String, Octopus]()

  val lastAddedRx: Var[Octopus] = Var {null}

  def put(octopus: Octopus): Unit = {
    cache.put(octopus.id, octopus)
    if (lastAddedRx.now != octopus) lastAddedRx() = octopus
  }

  def get(id: String): Option[Octopus] = cache.get(id)
}
