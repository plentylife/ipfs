package life.plenty.model.octopi.definition

trait Module[+T <: Hub] {
  val withinOctopus: T
}