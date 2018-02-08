package life.plenty.model.actions

import life.plenty.model.connection.Connection
import life.plenty.model.octopi.definition.{Module, Octopus}

trait ActionOnNew[T <: Octopus] extends Module[T] {
  def onNew()
}

trait ActionOnAddToModuleStack[T <: Octopus] extends Module[T] {
  def onAddToStack()
}

trait ActionOnGraphTransform extends ActionGraphTransform

trait ActionGraphTransform extends Module[Octopus] {
  def onConnectionAdd(connection: Connection[_]): Either[Exception, Unit]

  def onConnectionRemove(connection: Connection[_]): Either[Exception, Unit]
}

trait ActionAfterGraphTransform extends ActionGraphTransform

trait ActionOnConnectionsRequest extends Module[Octopus] {
  def onConnectionsRequest()
}