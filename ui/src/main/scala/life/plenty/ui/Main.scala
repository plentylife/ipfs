package life.plenty.ui

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.data.{OctopusReader, Main ⇒ dataMain}
import life.plenty.model.octopi.{BasicSpace, Octopus}
import life.plenty.model.{initialize ⇒ mInit}
import life.plenty.ui.display.Help
import life.plenty.ui.model.DisplayModel
import org.scalajs.dom.{Event, document}
import org.scalajs.dom.raw.Node

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scalaz.std.list._


@JSExportTopLevel("Main")
object Main {

  @JSExport
  def main(): Unit = {
    println("Entry point")

    dataMain.main()


    //    Router.initialize
    initialize()
    //
    val ts = new BasicSpace("test")
    OctopusReader.read(ts.id) foreach (s ⇒
      dom.render(document.body, mainSection(s))
      )

  }

  //  @JSExport
  //  def gun = dataMain.gun
  //
  //  @JSExport
  //  def hashes = Hashes
  //
  //  @JSExport
  //  def hashes256 = new Hashes.SHA256

  @dom
  def mainSection(space: Option[Octopus]): Binding[Node] = {
    //    val space = TestInstances.getEntry()


    //    val space =

    <div id="viewport" onclick={e: Event ⇒ Help.triggerClose()}>
      {Help.display.bind}{if (space.nonEmpty) DisplayModel.display(space.get).bind else <span>nothing to show</span>}
    </div>
  }

}
