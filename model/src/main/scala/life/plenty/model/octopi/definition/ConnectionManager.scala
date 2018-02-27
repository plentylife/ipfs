package life.plenty.model.octopi.definition

import scala.concurrent.ExecutionContext.Implicits.global
import life.plenty.model
import life.plenty.model.actions.{ActionAfterGraphTransform, ActionCatchGraphTransformError, ActionOnGraphTransform}
import life.plenty.model.connection.{DataHub, Id}
import rx.{Rx, Var}
import model._

import scala.concurrent.Future
import scala.util.{Failure, Success}

trait ConnectionManager[CT] {self: Hub ⇒
  private var onConnectionAddedOperations: List[(DataHub[_]) ⇒ Unit] = List()
  protected lazy val _connections: Var[List[DataHub[_]]] = Var(List.empty[DataHub[_]])

  protected def onConnectionAddedOperation(op: (DataHub[_]) ⇒ Unit): Unit = {
    onConnectionAddedOperations +:= op
  }

  def connections: Rx[List[DataHub[_]]] = _connections

  object sc {
    def all: List[DataHub[_]] = _connections.now

    def get[T](f: PartialFunction[DataHub[_], DataHub[T]]): Option[DataHub[T]] = sc.all.collectFirst(f)

    def ex[T](f: PartialFunction[DataHub[_], T]): Option[T] = sc.all.collectFirst(f)

    def exf[T](f: PartialFunction[DataHub[_], T]): T = ex(f).get
  }

  private lazy val actionsOnGraphTransform = Stream(getModules({ case m: ActionOnGraphTransform ⇒ m }): _*)
  private lazy val actionsAfterGraphTransform = Stream(getModules({ case m: ActionAfterGraphTransform ⇒ m }): _*)
  private lazy val actionCatchGraphTransformError =
    Stream(getModules({ case m: ActionCatchGraphTransformError ⇒ m }): _*)
  private var connectionCounter = -1

  def addConnection(connection: DataHub[_]): Future[Unit] = synchronized {
    console.println(s"~ ${this.getClass.getSimpleName} " +
      s"${sc.all.collectFirst({case Id(i) ⇒ i}).getOrElse("*")}\n" +
      s"\t<-- ${connection.getClass.getSimpleName} " +
      s"${connection.id}")

    // duplicates are silently dropped
    val existing = sc.all.find {c: DataHub[_] ⇒ c.id == connection.id}
    if (existing.nonEmpty) {
      console.trace(s"found existing connection ${existing.get} ${existing.get.id}")
      existing.get.activate()
      return Future{}
    }

    var onErrorList = Future.sequence(actionsOnGraphTransform map { m ⇒
      m.onConnectionAdd(connection)
    })

    onErrorList.transformWith {
      case Failure(e: Throwable) ⇒
        actionCatchGraphTransformError.foreach(_.catchError(e))
        console.error(s"Failed to add connection ${connection.id}")
        console.error(e)
        onErrorList map {_ ⇒ Unit}

      case Success(_) ⇒
        console.trace(s"Adding connection; check is Success ${this.getClass.getSimpleName} <-- ${connection} " +
          s"${connection.id}")
        connectionCounter += 1
        connection.setHolder(this)

        _connections() = connection :: _connections.now
        onConnectionAddedOperations.foreach(f ⇒ f(connection))

        onErrorList = Future.sequence(actionsAfterGraphTransform map { m ⇒
          m.onConnectionAdd(connection)
        })
        onErrorList onComplete {
          case Failure(e) ⇒
            actionCatchGraphTransformError.foreach(_.catchError(e))
            e
          case Success(_) ⇒
        }
        onErrorList map {_ ⇒ Unit}
    }
  }



}
