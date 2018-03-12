package life.plenty.ui

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.data.{DbReader, DbReaderModule, ShareDB, Main ⇒ dataMain}
import life.plenty.model.octopi._
import life.plenty.model.octopi.definition.Hub
import life.plenty.model.{defaultCreator_=, console ⇒ modelConsole, initialize ⇒ mInit}
import life.plenty.ui.display.actions.CreateSpace
import life.plenty.ui.display.{Help, LoadIndicator, Login, Modal}
import life.plenty.ui.model._
import life.plenty.ui.supplemental.{CriticalQuestionsGuide, IntroTutorial}
import life.plenty.{data, ui}
import org.scalajs.dom.raw.Node
import org.scalajs.dom.{Event, document}
import rx.Ctx

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scalaz.std.list._

@JSExportTopLevel("Main")
object Main {

  @JSExport
  def main(db: ShareDB, consolesActive: String): Unit = {
    println("Entry point")
    if (consolesActive.nonEmpty) {
      data.console.active = consolesActive == "true"
      ui.console.active = consolesActive == "true"
      modelConsole.active = consolesActive == "true"
    }

    // has to be first because it sets the hasher function
    dataMain.main(db)
    Router.initialize
    mInit()
    initialize()

    dom.render(document.getElementById("body-container"), mainSection())

    UiContext.devLogin
  }

  @dom
  def showUi(): Binding[Node] = {
    <span style="display:none">
      {if (UiContext.userVar.bind != null) {
      println(s"Setting default creator to ${UiContext.userVar.bind}")
      defaultCreator_=(UiContext.userVar.bind)

//      IntroTutorial(UiContext.userVar.bind)
      CriticalQuestionsGuide.apply()

      Router.router.state.bind.spaceId match {
        case Some(id) ⇒
          println(s"UI loading ${id}")
          LoadIndicator.forceOpen()
          DbReader.read(id) foreach { space ⇒
            println(s"UI loaded $id as $space")
            UiContext.setStatingSpace(space.asInstanceOf[Space])
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
      {Modal.display().bind}{LoadIndicator.show().bind}{showUi().bind}{Help.display().bind}{Login.display().bind}{
      //
      if(UiContext.startingSpace.bind.nonEmpty) DisplayModel.display(UiContext.startingSpace.bind.get).bind //
      else <span style="position: fixed; top: 0; left: 0">
        Opening up...
      </span>}
    </div>
  }


  @JSExport
  def console() = ui.console.active = true

  @JSExport
  def unloaded() = {
    data.Cache.hubCache.foreach(entry ⇒ {
      val (_, o) = entry
      o.getTopModule({ case m: DbReaderModule ⇒ m }).foreach {
        m ⇒
          if (m.connectionsLeftToLoad.now > 0) {
            println(s"${m.connectionsLeftToLoad} ${m.hub} ${m.hub.id}")
          }
      }
    })
  }

  private implicit val ctx = Ctx.Owner.safe()

  @JSExport
  def logConnections(of: String) = {
    val h = data.Cache.getOctopus(of) orElse data.Cache.getDataHub(of)
    println(h.get.rx.cons.now.mkString("\n"))
    println("raw")
    println(s"${
      h.get.sc.all.map(c ⇒
        s"$c ${c.id} ${
          if (c.value.isInstanceOf[Hub]) c.value.asInstanceOf[Hub].id else ""
        }").mkString("\n")
    }")
  }

  @JSExport
  def inCache() = {
    val cs = data.Cache.hubCache.values.toList ::: data.Cache.dataHubCache.values.toList
    for (c ← cs) {
      println(c)
    }
  }

  @JSExport
  def getUrl(spaceId: String) = Router.toHash(Router.defaultRoutingParams.copy(spaceId = Option(spaceId)))
}
