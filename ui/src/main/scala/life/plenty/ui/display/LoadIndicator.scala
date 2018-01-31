package life.plenty.ui.display

import com.thoughtworks.binding.Binding.{Var ⇒ bVar}
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.data.OctopusGunReaderModule
import life.plenty.{data, ui}
import org.scalajs.dom.raw.Node
import rx.{Ctx, Rx, Var}

object LoadIndicator {
  private implicit var ctx = Ctx.Owner.safe()

  private val connectionsLeft = bVar(0)

  val listOfModules = Var(List[OctopusGunReaderModule]())

  data.Cache.lastAddedRx.foreach { o ⇒
    if (o != null) {
      ui.console.println(s"LI added module ${o}")
      o.onModulesLoad {
        o.getTopModule({ case r: OctopusGunReaderModule ⇒ r }).foreach { reader =>
          //          ui.console.println(s"LoadIndicator ${reader.connectionsLeftToLoad()}")
          listOfModules() = reader :: listOfModules.now
        }
      }
      ui.console.println(s"LoadIndicator ${listOfModules.now}")
    }
  }

  val left: Rx[Int] = listOfModules.map { list ⇒
    val mvs = list.map { m ⇒
      val v = m.connectionsLeftToLoad()
      println(s"LI ${m} $v ${m.withinOctopus} ${m.withinOctopus.id}")
      if (v > 0) {v} else 0
    }
    (0 :: mvs).sum
  }
  left.foreach(connectionsLeft.value_=)


  @dom
  def show(): Binding[Node] = {
    <div>
      Loading
      {connectionsLeft.bind.toString}
    </div>
  }
}
