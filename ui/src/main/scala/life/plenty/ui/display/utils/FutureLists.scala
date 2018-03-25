package life.plenty.ui.display.utils

import com.thoughtworks.binding.Binding.Vars

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FutureList[T](initial: Future[List[T]]) {
  val v = Vars[T]()

  update(initial)

  def update(f: Future[List[T]]) = {
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
}
