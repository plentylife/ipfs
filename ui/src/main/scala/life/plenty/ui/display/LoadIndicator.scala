package life.plenty.ui.display

import com.thoughtworks.binding.Binding.{Var ⇒ bVar}
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.data
import life.plenty.data.DbReaderModule
import org.scalajs.dom.raw.Node
import rx.{Ctx, Rx, Var}

object LoadIndicator {
  private implicit var ctx = Ctx.Owner.safe()

  val connectionsLeft = bVar(0)

  val listOfModules = Var(List[DbReaderModule]())

  //  lazy val cachedGunReader =

  data.Cache.lastAddedRx.foreach { o ⇒
    if (o != null) {
      o.onModulesLoad {
        o.getTopModule({ case r: DbReaderModule ⇒ r }).foreach { reader =>
          //          ui.console.println(s"LoadIndicator ${reader.connectionsLeftToLoad()}")
          listOfModules() = reader :: listOfModules.now
        }
      }
    }
  }

  val left: Rx[Int] = listOfModules.map { list ⇒
    val mvs = list.map { m ⇒
      val v = m.connectionsLeftToLoad()
      if (v > 0) {v} else 0
    }
    (0 :: mvs).sum
  }
  left.foreach(connectionsLeft.value_=)

  private def loadStr(end: Int): String = (0 to end).map(_ ⇒ ".").mkString

  private val classes = "load-indicator"

  @dom
  def show(): Binding[Node] = {
    <div class={if (connectionsLeft.bind <= 0) "d-none " + classes else classes}>
      <div class="d-inline-flex logo">
        <img src="images/plenty_logo-400.png"/>
      </div>
      <span class="loading-text d-inline-flex">
        Loading {loadStr(connectionsLeft.bind)}
      </span>
    </div>
  }
}
