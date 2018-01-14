package life.plenty.ui

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.{initialize ⇒ mInit}
import life.plenty.ui.filters.RouterModuleFilter
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

    /* try to do something like this again */

    val space = TestInstances.getEntry()
    val d = DisplayModel.display(space)
    val f = space.getAllModules({ case m: RouterModuleFilter ⇒ m })
    println("filters", f)
    val fb = f map {_.update}
    val whole: List[Binding[Node]] = fb ::: List(d)

    <span>
      {for (b ← whole) yield b.bind}
    </span>
  }

}
