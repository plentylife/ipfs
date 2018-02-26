package life.plenty.ui

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.data.{DbReader, DbReaderModule, ShareDB, Main ⇒ dataMain}
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

    UiContext.devLogin
    // has to be first because it sets the hasher function
    dataMain.main(db)
    Router.initialize
    mInit()
    initialize()

    dom.render(document.body, mainSection())
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
        If you see this message it means that you are likely using Firefox
        <br/>
        Please be patient, wait 1+ minutes, and then refresh the page. Or use Chrome
        <br/>
        We are actively working to mitigate this serious issue
        <br/>
        Or maybe you have a wrong link...
      </span>}
    </div>
  }


  @JSExport
  def console() = ui.console.active = true

  @JSExport
  def unloaded() = {
    data.Cache.octopusCache.foreach(entry ⇒ {
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
    println(data.Cache.getOctopus(of).get.rx.cons.now.mkString("\n"))
    println("raw")
    println(s"${
      data.Cache.getOctopus(of).get.sc.all.map(c ⇒
        s"$c ${c.id} ${
          if (c.value.isInstanceOf[Hub]) c.value.asInstanceOf[Hub].id else ""
        }").mkString("\n")
    }")
  }

}
