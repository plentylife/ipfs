package life.plenty.model.octopi.definition

trait Module[+T <: Hub] {
  val hub: T
}