package life.plenty.ui

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.{initialize ⇒ mInit}
import life.plenty.ui.model.{DisplayModel, Router, RoutingParams}
import org.scalajs.dom.document
import org.scalajs.dom.raw.Node

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}


@JSExportTopLevel("Main")
object Main {

  @JSExport
  def main(): Unit = {
    println("Entry point")
    mInit()
    initialize()

    //    println("testing routing hash", Router.toHash(RoutingParams(ViewState.DISCUSSION)))
    println("testing routing hash", Router.toHash(RoutingParams(0)))

    dom.render(document.body, mainSection)
  }

  def mainSection: Binding[Node] = {
    val space = TestInstances.getEntry()
    DisplayModel.display(space)
  }

}
