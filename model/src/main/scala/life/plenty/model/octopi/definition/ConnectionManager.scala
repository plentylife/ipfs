package life.plenty.model.octopi.definition

import life.plenty.model.actions.{ActionAfterGraphTransform, ActionOnGraphTransform}
import life.plenty.model.connection.DataHub
import rx.{Rx, Var}

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

  def addConnection(connection: DataHub[_]): Either[Exception, Unit] = synchronized {
    // duplicates are silently dropped // fixme. this might not be working
    if (sc.all.contains {c: DataHub[_] ⇒ c.id == connection.id}) {
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
