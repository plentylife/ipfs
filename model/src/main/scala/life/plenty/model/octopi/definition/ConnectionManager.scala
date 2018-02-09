package life.plenty.model.octopi.definition

import life.plenty.model.actions.{ActionAfterGraphTransform, ActionOnGraphTransform}
import life.plenty.model.connection.Connection
import rx.{Rx, Var}

trait ConnectionManager[CT] {self: Octopus ⇒
  private var onConnectionAddedOperations: List[(Connection[_]) ⇒ Unit] = List()
  protected lazy val _connections: Var[List[Connection[_]]] = Var(List.empty[Connection[_]])

  protected def onConnectionAddedOperation(op: (Connection[_]) ⇒ Unit): Unit = {
    onConnectionAddedOperations +:= op
  }

  def connections: Rx[List[Connection[_]]] = _connections

  object sc {
    def all: List[Connection[_]] = _connections.now

    def get[T](f: PartialFunction[Connection[_], Connection[T]]): Option[Connection[T]] = sc.all.collectFirst(f)

    def ex[T](f: PartialFunction[Connection[_], T]): Option[T] = sc.all.collectFirst(f)

    def exf[T](f: PartialFunction[Connection[_], T]): T = ex(f).get
  }

  private lazy val actionsOnGraphTransform = Stream(getModules({ case m: ActionOnGraphTransform ⇒ m }): _*)
  private lazy val actionsAfterGraphTransform = Stream(getModules({ case m: ActionAfterGraphTransform ⇒ m }): _*)

  def addConnection(connection: Connection[_]): Either[Exception, Unit] = {
    // duplicates are silently dropped
    if (sc.all.contains {c: Connection[_] ⇒ c.id == connection.id}) {
      return Right()
    }

    var onErrorList = actionsOnGraphTransform map { m ⇒
      m.onConnectionAdd(connection)
    }

    onErrorList.collectFirst({ case e: Left[Exception, Unit] ⇒ e }) match {
      case Some(e) ⇒ e

      case None ⇒
        _connections() = connection :: _connections.now
        onConnectionAddedOperations.foreach(f ⇒ f(connection))

        onErrorList = actionsAfterGraphTransform map { m ⇒
          m.onConnectionAdd(connection)
        }
        onErrorList.collectFirst({ case e: Left[Exception, Unit] ⇒ e }) match {
          case Some(e) ⇒ e
          case None ⇒ Right()
        }
    }
  }

}
