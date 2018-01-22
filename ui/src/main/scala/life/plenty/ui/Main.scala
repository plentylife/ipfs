package life.plenty.ui

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.data.{Main ⇒ dataMain}
import life.plenty.model.{initialize ⇒ mInit}
import life.plenty.ui.display.Help
import life.plenty.ui.model.DisplayModel
import org.scalajs.dom.Event
import org.scalajs.dom.raw.Node

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scalaz.std.list._


@JSExportTopLevel("Main")
object Main {

  @JSExport
  def main(): Unit = {
    println("Entry point")

    dataMain.main()


    //    Router.initialize
    //    mInit()
    //    initialize()
    //
    //    dom.render(document.body, mainSection)
  }

  @JSExport
  def gun = dataMain.gun

  @dom
  def mainSection: Binding[Node] = {
    val space = TestInstances.getEntry()

    <div id="viewport" onclick={e: Event ⇒ Help.triggerClose()}>
      {Help.display.bind}{DisplayModel.display(space).bind}
    </div>
  }

}
