package life.plenty.model.hub.definition

trait Module[+T <: Hub] {
  val hub: T
}