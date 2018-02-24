package life.plenty.data

import life.plenty.data
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.JSGlobal


@js.native
trait ShareDB extends js.Object {
  def get(collection: String, id: String): ShareDBDoc = js.native
}

@js.native
trait ShareDBDoc extends js.Object {
  val `type`: String = js.native
  def fetch(errorCb: js.Function1[js.Object, Unit]): Unit = js.native
  def subscribe(errorCb: js.Function1[js.Object, Unit]): Unit = js.native
  def create(data: js.Object, errorCb: js.Function1[js.Object, Unit])
}

@js.native
@JSGlobal
object ShareDB extends ShareDB

class AsyncShareDoc(id: String, doSubscribe: Boolean = false) {
  private val db = ShareDB
  private val doc = db.get("plenty-docs", id)
  private var subscription: Future[Unit] = null

  // load right away
  if (doSubscribe) subscribe

  def exists: Future[Boolean] = fetch.map(_ ⇒ doc.`type` != null).recover {
    case e: Throwable ⇒ data.console.error(e); false
  }

  def subscribe: Future[Unit] = {
    if (subscription == null) {
      subscription = errCbToFuture(doc.subscribe)
    }
    subscription
  }

  def setInitial(info: ⇒ js.Object): Future[Unit] = subscription flatMap {_⇒
    if (doc.`type` == null) {
      data.console.trace(s"Creating doc with data ${JSON.stringify(info)}")
      create(info)
    } else Future(Unit)
  }

  def create(data: js.Object) = {
    def curry(cb: js.Function1[js.Object, Unit]) = doc.create(data, cb)
    errCbToFuture(curry)
  }

  def fetch = {
    data.console.trace(s"fetching $id")
    errCbToFuture(doc.fetch)
  }

  private def errCbToFuture(f: (js.Function1[js.Object, Unit]) ⇒ Unit): Future[Unit] = {
    val p = Promise[Unit]()
    def cb(e: js.Object) = {
      if (js.isUndefined(e) || e == null) p.success()
      else p.failure(new ShareDbError(JSON.stringify(e)))
    }

    f(cb)
    p.future
  }
}

class ShareDbError(json: String) extends Exception