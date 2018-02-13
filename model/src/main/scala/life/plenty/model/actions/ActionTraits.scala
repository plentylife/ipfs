package life.plenty.model.actions

import life.plenty.model.connection.DataHub
import life.plenty.model.octopi.definition.{Module, Hub}

trait ActionOnNew[T <: Hub] extends Module[T] {
  def onNew()
}

trait ActionOnAddToModuleStack[T <: Hub] extends Module[T] {
  def onAddToStack()
}

trait ActionOnGraphTransform extends ActionGraphTransform

trait ActionGraphTransform extends Module[Hub] {
  def onConnectionAdd(connection: DataHub[_]): Either[Exception, Unit]

  def onConnectionRemove(connection: DataHub[_]): Either[Exception, Unit]
}

trait ActionAfterGraphTransform extends ActionGraphTransform

trait ActionOnConnectionsRequest extends Module[Hub] {
  def onConnectionsRequest()
}