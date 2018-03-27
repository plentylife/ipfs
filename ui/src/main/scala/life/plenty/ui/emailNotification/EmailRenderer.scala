package life.plenty.ui.emailNotification

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.data.{DbReader, DbReaderModule, ShareDB, Main ⇒ dataMain}
import life.plenty.model.hub.User
import life.plenty.model.hub.definition.Hub
import life.plenty.model.utils.GraphEx._
import life.plenty.model.{console ⇒ modelConsole, initialize ⇒ mInit}
import life.plenty.ui.display.UserLayout
import life.plenty.ui.model._
import life.plenty.{data, ui}
import org.scalajs.dom.document
import org.scalajs.dom.raw.Node
import rx.Ctx
import scalaz.std.list._

import scala.concurrent.ExecutionContext.Implicits.{global ⇒ exg}
import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.Dynamic.global
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("Email")
object EmailRenderer {

  private def emailNext = global.emailNextUserEmail
  private def ready = global.readyToEmailUser

  @JSExport
  def render(db: ShareDB): Unit = {
    if (js.isUndefined(emailNext) || js.isUndefined(ready)) {
      println("Missing interface functions for sending emails")
      return
    }

    println("Creating email")
    data.console.active = false
    ui.console.active = false
    modelConsole.active = false

    // has to be first because it sets the hasher function
    dataMain.main(db)
    mInit()
    ui.initialize()

    EmailStatusManager.turnOn()

    next()
  }

  private var currentUser: User = null

  def send() = {
    val uName = getName(currentUser)
    val uEmail = getEmail(currentUser)
    for (n ← uName; e ← uEmail) {
      ready(e.get, n.get)
    }
  }

  def next() = {
    emailNext
    DbReader.read("yv5_7GVnoTxA-DZ_DNvOQ6NhaRJAN_wbfoXpRfdEy8SSAnoMGooxYg") foreach { hub ⇒
      currentUser = hub.asInstanceOf[User]
      dom.render(document.getElementById("body-container"), emailDom(hub.asInstanceOf[User]))
    }
  }

  @dom
  def emailDom(user: User): Binding[Node] = {
    <div id="viewport">
      {UserLayout.html(user).bind}
    </div>
  }
}
