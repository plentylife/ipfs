package life.plenty.model.actions

import life.plenty.model.octopi.{Module, Octopus}

trait ActionOnNew[T <: Octopus] extends Module[T] {
  def onNew()
}

trait ActionOnAddToModuleStack[T <: Octopus] extends Module[T] {
  def onAddToStack()
}
