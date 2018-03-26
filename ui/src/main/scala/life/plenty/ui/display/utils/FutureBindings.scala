package life.plenty.ui.display.utils

import com.thoughtworks.binding.Binding.{Var, Vars}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait FutureVar[T] {
  val v = Var[Option[T]](None)

  //  val getter: () ⇒ Future[T]
  //
  //  update
  //
  //  def set(f: Future[T])
  //
  //  def update = set(getter())
}


class FutureNakedVar[T](getter: ⇒ Future[T]) extends FutureVar[T] {
  update

  def set(f: Future[T]) = f foreach (n ⇒ v.value_=(Option(n)))

  def update = set(getter)
}


class FutureOptVar[T](getter: ⇒ Future[Option[T]]) extends FutureVar[T] {
  update

  def set(f: Future[Option[T]]) = f foreach (n ⇒ v.value_=(n))

  def update = set(getter)
}

class FutureList[T](getter: ⇒ Future[List[T]]) {
  val v = Vars[T]()

  update

  def set(f: Future[List[T]]) = {
    f.foreach(l ⇒ {
      var current = v.value
      var i = 0
      current zip l foreach { z ⇒
        val (o, n) = z
        if (o != n) {
          v.value.insert(i, n)
        }
        i += 1
      }

      current = v.value
      if (current.size > l.size) {
        current.remove(l.size, current.size - l.size)
      } else if (l.size > current.size) {
        val ns = l.slice(current.size, l.size)
        current.insertAll(current.size, ns)
      }

    })
  }

  def update = set(getter)
}
