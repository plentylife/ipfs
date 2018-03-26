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
import monix.execution.Scheduler.{global ⇒ mg}

object LoadIndicator {
  private implicit var ctx = Ctx.Owner.safe()

  val connectionsLeft = bVar(0)
  data.ReaderInterface.LoadIndicator.get.foreach(connectionsLeft.value_=)(mg)

  private def loadStr(end: Int): String = {
    (0 until end).map(_ ⇒ ".").mkString
  }

  private val classes = "load-indicator"

//  <div class={if (connectionsLeft.bind <= 0) "d-none " + classes else classes}>
//    <div class={classes}>

  //    <div class={if (connectionsLeft.bind > 0 || fo) classes else "d-none " + classes }>

  @dom
  def show(): Binding[Node] = {
    <div class={if (connectionsLeft.bind > 0) classes + " loaded" else "d-none " + classes }>
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
