package life.plenty.ui

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.data.{OctopusReader, Main ⇒ dataMain}
import life.plenty.model.octopi.{BasicSpace, Octopus, Space}
import life.plenty.model.{initialize ⇒ mInit}
import life.plenty.ui.display.Help
import life.plenty.ui.model.DisplayModel
import org.scalajs.dom.raw.Node
import org.scalajs.dom.{Event, document}

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
    mInit()
    initialize()


    val ts = new BasicSpace("test")
    println(s"ui loading ${ts.id}")
    OctopusReader.read(ts.id) foreach { spaceOpt ⇒
      spaceOpt foreach { s ⇒ UiContext.startingSpace = s.asInstanceOf[Space] }
      //      window.setTimeout(() ⇒ {
      //        println("*** after load")
      //        println(s.get.connections)
      //        Cache.cache.foreach{m ⇒ {
      //          val (id, o) = m
      //          println(s"${id} ${o.id} ${o}")
      //        }}
      //      }, 3000)

      dom.render(document.body, mainSection(spaceOpt))
    }
  }

  @dom
  def mainSection(space: Option[Octopus]): Binding[Node] = {
    <div id="viewport" onclick={e: Event ⇒ Help.triggerClose()}>
      {Help.display().bind}{if (space.nonEmpty) DisplayModel.display(space.get).bind else <span>nothing to show</span>}
    </div>
  }

}
