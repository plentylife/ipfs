package life.plenty.ui.display.utils

import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding.{BindingSeq, Vars}
import life.plenty.model.hub.Space
import life.plenty.ui.display.feed.FeedModuleDirectory
import life.plenty.ui.model.{SimpleDisplayModule, SimpleDisplayModuleDirectory}
import org.scalajs.dom.Node
import rx.{Ctx, Rx}

abstract class DomRenderList[T](list: Rx[List[T]]) {
  private val inner = Vars[(T, Binding[Node])]()
  private var cache = Map[T, Binding[Node]]()

  implicit val ctx: Ctx.Owner = Ctx.Owner.safe()

  private def render(what: T): Option[Binding[Node]] = synchronized {
    cache get what match {
      case b @ Some(_) ⇒
        println(s"Updating map with existing ${what}")
        b
      case _ ⇒ val r = getRenderer(what) map {m ⇒ m.html(what)}
        r foreach {r ⇒ cache += (what → r)}
        r
    }
  }
  //dir: SimpleDisplayModuleDirectory[_]
  protected def getRenderer(what: T): Option[SimpleDisplayModule[T]]

  list.foreach(l ⇒ {
    val current = inner.value
    var i = 0
    current zip l foreach {z ⇒
      val (o, n) = z
      if (o._1 != n) {
        render(n) foreach {r ⇒ current.update(i, n → r)}
      }
      i += 1
    }
    if (current.size > l.size) {
      current.remove(l.size, current.size - l.size)
    } else if (l.size > current.size) {
      val ns = l.slice(current.size, l.size) map {n ⇒ n → render(n)} collect {case (n, Some(r)) ⇒ n -> r}
      current.insertAll(current.size, ns)
    }

  })

  def apply(): BindingSeq[Binding[Node]] = inner.map(_._2)
}

class DomRenderListSingleModule[T](list: Rx[List[T]], simpleDisplayModule: SimpleDisplayModule[T]) extends DomRenderList[T](list) {
  override protected def getRenderer(what: T): Option[SimpleDisplayModule[T]] = Option(simpleDisplayModule)
}
