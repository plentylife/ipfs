package life.plenty.ui

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.data.{OctopusGunReaderModule, OctopusReader, Main ⇒ dataMain}
import life.plenty.model.octopi._
import life.plenty.model.{defaultCreator_=, console ⇒ modelConsole, initialize ⇒ mInit}
import life.plenty.ui.display.{Help, LoadIndicator, Login}
import life.plenty.ui.model.{DisplayModel, Router, UiContext}
import life.plenty.{data, ui}
import org.scalajs.dom.raw.Node
import org.scalajs.dom.{Event, document}
import rx.Ctx

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scalaz.std.list._

@JSExportTopLevel("Main")
object Main {

  @JSExport
  def main(bootstrapPeers: js.Array[String], consolesActive: String): Unit = {
    println("Entry point")
    if (consolesActive.nonEmpty) {
      data.console.active = consolesActive == "true"
      ui.console.active = consolesActive == "true"
      modelConsole.active = consolesActive == "true"
    }
    println(ui.console.active, consolesActive, consolesActive.nonEmpty)

    // has to be first because it sets the hasher function
    dataMain.main(bootstrapPeers)
    UiContext.initialize()
    Router.initialize
    mInit()
    initialize()

    dom.render(document.body, mainSection())
    //    if (UiContext.getUser != null) showUi()
  }

  @dom
  def showUi(): Binding[Node] = {
    //    val id = "02PgqlznC6LmE6FJNettMmLVxztTemvxdb3ChgXTsOk="

    <span style="display:none">
      {if (UiContext.userVar.bind != null) {
      println(s"Setting default creator to ${UiContext.userVar.bind}")
      defaultCreator_=(UiContext.userVar.bind)
      Router.router.state.bind.spaceId match {
        case Some(id) ⇒
          println(s"UI loading ${id}")
          OctopusReader.read(id) foreach { spaceOpt ⇒
            UiContext.startingSpace.value_=(spaceOpt map { s ⇒ s.asInstanceOf[Space] })
          }
        case None ⇒ println("Router params do not have a space id")
      }
      ""
    } else ""}
    </span>
  }

  @JSExport
  def newSpace() = {
    val ts = TestInstances.load()
    val rp = Router.defaultRoutingParams.copy(spaceId = Option(ts.id))
    println("Routing hash")
    println(Router.toHash(rp))
    Router.navigateToOctopus(ts)
  }

  @JSExport
  def log() = println(UiContext.startingSpace.value.get.connections)

  @JSExport
  def logDataCache() = println(data.Cache.octopusCache)

  @JSExport
  def clearDataCache() = data.Cache.octopusCache.clear()

  @JSExport
  def unloaded() = {
    data.Cache.octopusCache.foreach(entry ⇒ {
      val (_, o) = entry
      o.getTopModule({ case m: OctopusGunReaderModule ⇒ m }).foreach {
        m ⇒ println(s"${m.connectionsLeftToLoad} ${m.withinOctopus} ${m.withinOctopus.id}")
      }
    })
  }

  @JSExport
  def loadIndicator() = {
    println(LoadIndicator.left)
    LoadIndicator.listOfModules.now.foreach(m ⇒ {
      println(s"${m.connectionsLeftToLoad} ${m.withinOctopus} ${m.withinOctopus.id}")
    })
  }

  @JSExport
  def logSpecMods() = {
    data.Cache.octopusCache.get("GDvfPd9a337klT/if8J7BLzZDbyTuPNjZ/gI+8/kfuU=")
  }

  private implicit val ctx = Ctx.Owner.safe()

  @JSExport
  def logConnections(of: String) = {
    println(data.Cache.getOctopus(of).get.rx.cons.now.mkString("\n"))
    println("raw")
    println(s"${
      data.Cache.getOctopus(of).get.connections.map(c ⇒ c → {
        if (c.value.isInstanceOf[Octopus]) c.value
          .asInstanceOf[Octopus].id else ""
      }).mkString("\n")
    }")
  }


  @dom
  def mainSection(): Binding[Node] = {
    <div id="viewport" onclick={e: Event ⇒ Help.triggerClose()}>
      {LoadIndicator.show().bind}
      {showUi().bind}
      {Help.display().bind}{Login.display().bind}{if (UiContext.startingSpace.bind.nonEmpty)
      DisplayModel.display(UiContext.startingSpace.bind.get).bind else
      <span>nothing to show</span>}
    </div>
  }

}
