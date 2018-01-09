package life.plenty.model.actions

import life.plenty.model.{Module, Octopus}

trait ActionOnInitialize[T <: Octopus] extends Module[T] {
  def onInitialize()
}
