package life.plenty.ui.display

import com.thoughtworks.binding.Binding.{Var ⇒ bVar}
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.data
import life.plenty.data.OctopusGunReaderModule
import org.scalajs.dom.raw.Node
import rx.{Ctx, Rx, Var}

object LoadIndicator {
  private implicit var ctx = Ctx.Owner.safe()

  val connectionsLeft = bVar(0)

  val listOfModules = Var(List[OctopusGunReaderModule]())

  //  lazy val cachedGunReader =

  data.Cache.lastAddedRx.foreach { o ⇒
    if (o != null) {
      o.onModulesLoad {
        o.getTopModule({ case r: OctopusGunReaderModule ⇒ r }).foreach { reader =>
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

  //      {for(i <- 0 until connectionsLeft.bind) yield "."}

  private def loadStr(end: Int): String = (0 to end).map(_ ⇒ ".").mkString

  @dom
  def show(): Binding[Node] = {
    <div class={if (connectionsLeft.bind <= 0) "d-none" else ""}>
      Loading
      {loadStr(connectionsLeft.bind)}
    </div>
  }
}
