package life.plenty.model.octopi.definition

trait Module[+T <: Octopus] {
  val withinOctopus: T
}