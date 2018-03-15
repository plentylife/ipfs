package life.plenty.model.actions

import life.plenty.model.connection.DataHub
import life.plenty.model.hub.definition.{Hub, Module}

import scala.concurrent.Future

trait ActionOnNew[T <: Hub] extends Module[T] {
  def onNew()
}

trait ActionOnAddToModuleStack[T <: Hub] extends Module[T] {
  def onAddToStack()
}

trait ActionCatchGraphTransformError extends Module[Hub] {
  def catchError(e: Throwable)
}

trait ActionOnGraphTransform extends ActionGraphTransform

trait ActionGraphTransform extends Module[Hub] {
  def onConnectionAdd(connection: DataHub[_]): Future[Unit]
}

trait ActionAfterGraphTransform extends ActionGraphTransform

trait ActionOnConnectionsRequest extends Module[Hub] {
  def onConnectionsRequest()
}

trait ActionOnFinishDataLoad extends Module[Hub] {
  def onFinishLoad(f: () ⇒ Unit): Unit
}