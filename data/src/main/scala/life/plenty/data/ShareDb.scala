package life.plenty.data

import life.plenty.data

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.JSGlobal
import scala.util.Try


@js.native
trait ShareDB extends js.Object {
  def get(collection: String, id: String): ShareDBDoc = js.native
}

@js.native
trait ShareDBDoc extends js.Object {
  val `type`: js.Any = js.native // is an object actually
  val data: js.Object = js.native

  def fetch(errorCb: js.Function1[js.Object, Unit]): Unit = js.native
  def subscribe(errorCb: js.Function1[js.Object, Unit]): Unit = js.native
  // missing options
  def create(data: js.Object, errorCb: js.Function1[js.Object, Unit]): Unit = js.native
  // missing options
  def submitOp(op: js.Object, errorCb: js.Function1[js.Object, Unit]): Unit = js.native
  // this does not fit all of them. some are not js.F2
  def on(channel: String, cb: js.Function2[js.Array[NativeDbOp], Boolean, Unit]): Unit = js.native
}

trait DbOp extends js.Object {
  val p: js.Array[js.Any]
}
trait DbInsertOp extends DbOp {
  val li: js.Any
}

@js.native
trait NativeDbOp extends DbOp
@js.native
trait NativeDbInsertOp extends DbInsertOp

@js.native
@JSGlobal
object ShareDB extends ShareDB

class DocWrapper(id: String) {
  private val db = ShareDB
  private val doc = db.get("plenty-docs", id)
  private var subscription: Future[Unit] = null

  // load right away
//  if (doSubscribe) subscribe

  def getData: Future[JsHub] = exists map { doesExist ⇒
    if (doesExist) doc.data.asInstanceOf[JsHub] else throw new DocDoesNotExist(id) // fixme. shouldn't throw
  }

  def exists: Future[Boolean] = Option(subscription).getOrElse {
    // no point double fetching
    if (doc.`type` != null) Future() else fetch
  }.map(_ ⇒ doc.`type` != null).recover {
    case e: Throwable ⇒ data.console.error(e); false
  }

  def subscribe: Future[Unit] = {
    if (subscription == null) {
      console.trace(s"Doc subscribing to $id")
      subscription = errCbToFuture(doc.subscribe)
    }
    subscription
  }

  private def opListener: js.Function2[DbOp, Boolean, Unit] = null
  def onRemoteConnectionChange(idCb: String ⇒ Unit) = {
    if (opListener == null) {
      doc.on("op", (ops: js.Array[NativeDbOp], source: Boolean) ⇒ {
        console.trace(s"Doc listener fired $source ${JSON.stringify(ops)}")
        ops foreach { op ⇒
          if (!source) {
            Try {op.asInstanceOf[NativeDbInsertOp]} foreach (insOp ⇒ {
              if (insOp.p.headOption.exists(_.toString == "connections")) idCb(insOp.li.toString)
            })
          }
        }
      })
    }
  }

  def setInitial(info: ⇒ js.Object): Future[Unit] = {
    if (subscription != null) subscription else if (doc.`type` != null) Future() else fetch
  } flatMap {_⇒
    if (doc.`type` == null) {
      data.console.trace(s"Creating doc with data ${JSON.stringify(info)}")
      create(info)
    } else Future(Unit)
  }

  def create(data: js.Object): Future[Unit] = {
    def curry(cb: js.Function1[js.Object, Unit]) = doc.create(data, cb)
    errCbToFuture(curry)
  }

  def submitOp(op: DbOp) = {
    def curry(cb: js.Function1[js.Object, Unit]) = doc.submitOp(op, cb)
    errCbToFuture(curry)
  }

  def fetch = {
    // fixme this gets called twice // maybe just for secure user
    data.console.trace(s"Doc fetching $id $this")
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

class DocDoesNotExist(id: String) extends Exception
class ShareDbError(json: String) extends Exception