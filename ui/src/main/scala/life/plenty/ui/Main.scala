package life.plenty.ui

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.{initialize ⇒ mInit}
import life.plenty.ui.model.{DisplayModel, Router}
import org.scalajs.dom.raw.Node
import org.scalajs.dom.{document, window}

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scalaz.std.list._


@JSExportTopLevel("Main")
object Main {

  @JSExport
  def main(): Unit = {
    println("Entry point")
    println("Window hash", window.location.hash)

    Router.initialize
    mInit()
    initialize()

    dom.render(document.body, mainSection)
  }

  def mainSection: Binding[Node] = {
    val space = TestInstances.getEntry()
    DisplayModel.display(space)
  }

}
