package life.plenty.data

import life.plenty.model.connection.DataHub
import life.plenty.model.hub.definition.Hub
import life.plenty.model.interfaces.ReaderSpec
import monix.reactive.{MulticastStrategy, Observable}

import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

/** Used to load hubs and their connections */
object ReaderInterface extends ReaderSpec {
  object LoadIndicator {

    import monix.execution.Scheduler.Implicits.global

    private lazy val (liSub, loadObs) = Observable.multicast[Int](MulticastStrategy.publish)
    lazy val loadIndicator = loadObs.scan(0)(_ + _)

    def notify(by: Int) = liSub.onNext(by)
    def get = loadIndicator
  }

  def loadConnections(hub: Hub): Unit = hub.hasSetId foreach {_ ⇒
  val dbDoc = DocCache.get(hub)

    dbDoc.subscribe
    console.println(s"Reader loading ${hub} ${hub.id}")

    dbDoc.getData map { data ⇒
      val existingIds = hub.sc.all.map(_.id)
      // the reverse is important -- making sure that we are loading the oldest first
      val unloadedIds = data.connections.toList.filterNot(existingIds.contains).reverse

      console.trace(s"Reader has connections to load for $hub ${hub.id} $unloadedIds")

      LoadIndicator.notify(unloadedIds.size)
      var leftToLoad = unloadedIds.size
      if (leftToLoad <= 0) hub.loadHasComplete() // in case there are non to load
      unloadedIds map loadConnection foreach {_ foreach {
        c ⇒ console.trace(s"Reader loaded connection for ${hub} ${hub.id} -- $c")
          LoadIndicator.notify(-1); leftToLoad -= 1
          hub.addConnection(c)
          if (leftToLoad <= 0) hub.loadHasComplete()
      }}
    }

    def loadAndAdd(id: String) = {
      console.trace(s"Reader trying to load connection for ${hub} ${hub.id} with id ${id}")
      loadConnection(id) foreach hub.addConnection
    }
    dbDoc.onRemoteConnectionChange(loadAndAdd)
  }

  private def loadConnection(id: String): Future[DataHub[_]] = {
    val p = Promise[DataHub[_]]()
    // playing with setTimeout to allow for ui rendering
    js.timers.setTimeout(1) {

      DataHubReader.read(id) onComplete {
        case Success(c) ⇒
          console.trace(s"Reader loaded connection with id $id")
          p.success(c)
        case Failure(e) ⇒
          console.trace(s"Reader failed to load connection with id ${id}")
          console.error(e)
          e.printStackTrace()
          p.failure(e)
      }

    }

    p.future
  }

}
