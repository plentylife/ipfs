package life.plenty.ui.display

import com.thoughtworks.binding.Binding.{Var ⇒ bVar}
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.data
import life.plenty.data.DbReaderModule
import org.scalajs.dom.raw.Node
import rx.{Ctx, Rx, Var}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js

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
    val res = (0 :: mvs).sum

    if (res == 0) _forceOpen.value_=(false)

//    println(s"LOADING IND $res ${list.size} ${list.toSet.size}")

    res
  }
  left.foreach(connectionsLeft.value_=)

  private def loadStr(end: Int): String = {
    (0 until end).map(_ ⇒ ".").mkString
  }

  private lazy val _forceOpen = bVar(false)
  def forceOpen(): Unit = {
    _forceOpen.value_=(true)
    autoClose
  }

  private def autoClose: Unit = Future {
    js.timers.setTimeout(1000)({
      if (left.now <= 0) {
        _forceOpen.value_=(false)
      } else autoClose
    })
  }

  private val classes = "load-indicator"

//  <div class={if (connectionsLeft.bind <= 0) "d-none " + classes else classes}>
//    <div class={classes}>

  @dom
  def show(): Binding[Node] = {
    val fo = _forceOpen.bind

    <div class={if (connectionsLeft.bind > 0 || fo) classes else "d-none " + classes }>
      <div class="d-inline-flex logo">
        <img src="images/plenty_logo-400.png"/>
      </div>
      <span class="loading-text d-inline-flex">
        Loading
        {loadStr(connectionsLeft.bind)}
      </span>
    </div>
  }
}
