package life.plenty.ui

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.data.{OctopusGunReaderModule, OctopusReader, Main ⇒ dataMain}
import life.plenty.model.octopi._
import life.plenty.model.octopi.definition.Hub
import life.plenty.model.{defaultCreator_=, console ⇒ modelConsole, initialize ⇒ mInit}
import life.plenty.ui.display.actions.CreateSpace
import life.plenty.ui.display.{Help, LoadIndicator, Login, Modal}
import life.plenty.ui.model._
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

    // has to be first because it sets the hasher function
    dataMain.main(bootstrapPeers) foreach {_ ⇒
      UiContext.initialize()
      Router.initialize
      mInit()
      initialize()

      dom.render(document.body, mainSection())
    }
  }

  @dom
  def showUi(): Binding[Node] = {
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
        case None ⇒ CreateSpace.openInModal()
      }
      ""
    } else ""}
    </span>
  }

  @dom
  def mainSection(): Binding[Node] = {
    <div id="viewport" onclick={e: Event ⇒ Help.triggerClose()}>
      {Modal.display().bind}
      {LoadIndicator.show().bind}
      {showUi().bind}
      {Help.display().bind}{Login.display().bind}{if (UiContext.startingSpace.bind.nonEmpty)
      DisplayModel.display(UiContext.startingSpace.bind.get).bind else
      <span>nothing to show</span>}
    </div>
  }


  @JSExport
  def newSpace() = {
    val ts = TestInstances.load()
    val rp = Router.defaultRoutingParams.copy(spaceId = Option(ts.id))
    println("Routing hash")
    println(Router.toHash(rp))
//    Router.navigateToOctopus(ts)
  }

  @JSExport
  def console() = ui.console.active = true

  @JSExport
  def log() = println(UiContext.startingSpace.value.get.sc.all)

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

  private implicit val ctx = Ctx.Owner.safe()

  @JSExport
  def logConnections(of: String) = {
    println(data.Cache.getOctopus(of).get.rx.cons.now.mkString("\n"))
    println("raw")
    println(s"${
      data.Cache.getOctopus(of).get.sc.all.map(c ⇒ c → {
        if (c.value.isInstanceOf[Hub]) c.value
          .asInstanceOf[Hub].id else ""
      }).mkString("\n")
    }")
  }

  @JSExport
  def jd = jdenticon

  @JSExport
  def toHash(id: String) = Router.toHash(RoutingParams(0, Option(id)))

  @JSExport
  def gun = data.Main.gun
}
