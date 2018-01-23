package life.plenty.model.actions

import life.plenty.model.octopi.{Module, Octopus}

trait ActionOnInitialize[T <: Octopus] extends Module[T] {
  def onInitialize()
}

trait ActionOnAddToModuleStack[T <: Octopus] extends Module[T] {
  def onAddToStack()
}
