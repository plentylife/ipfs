package life.plenty.ui

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.ui.display.SpaceDisplay
import org.scalajs.dom.document
import org.scalajs.dom.raw.Node

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}


@JSExportTopLevel("Main")
object Main {

  @JSExport
  def main(): Unit = {
    dom.render(document.body, mainSection)
  }

  def mainSection: Binding[Node] = {
    SpaceDisplay.displayData(TestInstances.testSpace, TestInstances.connections)
  }

}
