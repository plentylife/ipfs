package life.plenty.model.hub.pseudo
import life.plenty.model.connection.DataHub

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

trait StrongHub {
  private var properties = List[StrongProperty[_]]()
  private var readyProperties = 0
  private var failed = false

  private[pseudo] def register[T](p: StrongProperty[T]) = {
    properties +:= p
    p.ready.onComplete {_ match {
      case Success(_) ⇒ readyProperties += 1
      case Failure(_) ⇒ failed = true
    }}
  }
  def ready: Future[this.type] = ???
}

trait StrongProperty[T] {
  val insideOf: StrongHub
  private var _value: T = _
  def set(v: T): Unit = {
    _value = v
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

case class ValStrongProperty[T](valValue: T, override val insideOf: StrongHub) extends StrongProperty[T] {
  set(valValue)
}