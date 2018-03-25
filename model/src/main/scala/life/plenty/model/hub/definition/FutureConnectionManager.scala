package life.plenty.model.hub.definition

import life.plenty.model.connection.DataHub
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}

trait FutureConnectionManager {self: Hub ⇒
  lazy val loadCompletePromise = Promise[Unit]()
//  lazy val loadCompleteF = loadComplete.future
  private var hasCalledDb = false

  // fixme just do a direct call to db singleton
  def loadCompleted: Future[Hub] = {
    if (!hasCalledDb) {
      onConnectionsRequest.foreach(f ⇒ f())
      hasCalledDb = true
    }
    loadCompletePromise.future.map(_ ⇒ self)
  }

  private[FutureConnectionManager] def cs = connections.now

  def get[T](extractor: PartialFunction[DataHub[_], T]): Future[Option[T]] = {
    // fixme check for active
    loadCompleted map {h ⇒ h.cs.collectFirst(extractor)}
  }

  def getAll[T](extractor: PartialFunction[DataHub[_], T]): Future[List[T]] = {
    // fixme check for active
    loadCompleted map {_.cs.collect(extractor)}
  }

  /** waits for the load */
//  private[FutureConnectionManager] def isActiveL =
}
