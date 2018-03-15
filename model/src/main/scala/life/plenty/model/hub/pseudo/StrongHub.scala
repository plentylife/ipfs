package life.plenty.model.hub.pseudo
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

trait StrongHub {
  private var properties = List[StrongProperty[_, _]]()
  private var readyProperties = 0
  private var failed = false

  private[pseudo] def register[T](p: StrongProperty[T, _]) = {
    properties +:= p
    p.ready.onComplete {_ match {
      case Success(_) ⇒ readyProperties += 1
      case Failure(_) ⇒ failed = true
    }}
  }
  def ready: Future[this.type] = ???
}

class StrongProperty[T, H <: StrongHub](insideOf: H) {
  private var _value: T = _
  def apply(v: T): H = {
    _value = v; insideOf
  }
  def value: T = {
//    readyPromise.future
    // fixme fail if promise not complete
    _value
  }

  private lazy val readyPromise = Promise[Unit]()
  private lazy val readyFuture = readyPromise.future
  def ready: Future[this.type] = readyFuture map {_ ⇒ this}
  private def register() = insideOf.register(this)

  register()
}