package life.plenty.ui

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.{initialize ⇒ mInit}
import life.plenty.ui.display.Help
import life.plenty.ui.model.{DisplayModel, Router}
import org.scalajs.dom.raw.Node
import org.scalajs.dom.{Event, document}

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scalaz.std.list._


@JSExportTopLevel("Main")
object Main {

  @JSExport
  def main(): Unit = {
    println("Entry point")

    Router.initialize
    mInit()
    initialize()

    dom.render(document.body, mainSection)
  }

  @dom
  def mainSection: Binding[Node] = {
    val space = TestInstances.getEntry()

    <div id="viewport" onclick={e: Event ⇒ Help.triggerClose()}>
      {Help.display.bind}{DisplayModel.display(space).bind}
    </div>
  }

}
