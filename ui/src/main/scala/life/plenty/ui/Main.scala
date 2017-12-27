package life.plenty.ui

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.ui.model.DisplayModuleDefinitions
import org.scalajs.dom.document
import org.scalajs.dom.raw.Node

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}


@JSExportTopLevel("Main")
object Main {

  @JSExport
  def main(): Unit = {
    println("Entry point")
    initialize()
    dom.render(document.body, mainSection)
  }

  def mainSection: Binding[Node] = {
    val space = TestInstances.frenchSpace
    DisplayModuleDefinitions.display(space)
  }

}
