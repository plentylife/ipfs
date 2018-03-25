package life.plenty.model.hub.definition

import life.plenty.model.connection.DataHub
import life.plenty.model.interfaces.ReaderSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}

trait FutureConnectionManager {self: Hub ⇒
  lazy val loadCompletePromise = Promise[Unit]()
//  lazy val loadCompleteF = loadComplete.future
  private var hasCalledDb = false

  /** Indicate to the hub that it has finished loading */
  def loadHasComplete(): Unit = loadCompletePromise.success()
  // fixme just do a direct call to db singleton
  def whenLoadComplete: Future[Hub] = {
    if (!hasCalledDb) {
      println(s"WLC")
      ReaderSpec.interface.loadConnections(this)
      hasCalledDb = true
    }
    loadCompletePromise.future.map(_ ⇒ self)
  }

  private[FutureConnectionManager] def cs = connections.now

  def get[T](extractor: PartialFunction[DataHub[_], T]): Future[Option[T]] = {
    // fixme check for active
    whenLoadComplete map { h ⇒ h.cs.collectFirst(extractor)}
  }

  def getAll[T](extractor: PartialFunction[DataHub[_], T]): Future[List[T]] = {
    // fixme check for active
    whenLoadComplete map {_.cs.collect(extractor)}
  }

  /** waits for the load */
//  private[FutureConnectionManager] def isActiveL =
}
