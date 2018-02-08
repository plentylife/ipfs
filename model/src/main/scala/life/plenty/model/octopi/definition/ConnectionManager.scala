package life.plenty.model.octopi.definition

import life.plenty.model.connection.Connection
import rx.Var

trait ConnectionManager[CT] {
  protected lazy val _connections: Var[List[Connection[_]]] = Var(List.empty[Connection[_]])

  object sc {
    def all: List[Connection[_]] = _connections.now

    def get[T](f: PartialFunction[Connection[_], Connection[T]]): Option[Connection[T]] = sc.all.collectFirst(f)

    def ex[T](f: PartialFunction[Connection[_], T]): Option[T] = sc.all.collectFirst(f)

    def exf[T](f: PartialFunction[Connection[_], T]): T = ex(f).get
  }

}
