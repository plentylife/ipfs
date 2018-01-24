package life.plenty.data

import life.plenty.model.octopi.Octopus

import scala.collection.mutable

object Cache {
  val cache = mutable.Map[String, Octopus]()

  def put(octopus: Octopus): Option[Octopus] = cache.put(octopus.id, octopus)

  def get(id: String): Option[Octopus] = cache.get(id)
}
