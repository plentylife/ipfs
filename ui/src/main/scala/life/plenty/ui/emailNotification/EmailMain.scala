package life.plenty.ui.emailNotification

import com.thoughtworks.binding.Binding.{BindingSeq, Vars}
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
object EmailMain {

  private def emailNext = global.emailNextUserEmail
  private def ready = global.readyToEmailUser
  private var baseUrl: String = _

  @JSExport
  def render(db: ShareDB, _baseUrl: String): Unit = {
    if (js.isUndefined(emailNext) || js.isUndefined(ready)) {
      println("Missing interface functions for sending emails")
      return
    }

    baseUrl = _baseUrl
    data.console.active = false
    ui.console.active = false
    modelConsole.active = false

    // has to be first because it sets the hasher function
    dataMain.main(db)
    mInit()
    ui.initialize()

    EmailManager.turnOn()

    next()
  }

  private var currentUser: User = null

  def send(): Unit = {
    val uName = getName(currentUser)
    val uEmail = getEmail(currentUser)
    for (n ← uName; e ← uEmail) {
      ready(e.get, n.get)
    }
  }

  def next(): Unit = {
    emailNext
    DbReader.read("yv5_7GVnoTxA-DZ_DNvOQ6NhaRJAN_wbfoXpRfdEy8SSAnoMGooxYg") foreach { hub ⇒
      currentUser = hub.asInstanceOf[User]

      val bindings = Vars[Binding[Node]]()
      dom.render(document.getElementById("body-container"), emailDom(hub.asInstanceOf[User], bindings))

      EmailBuilder.renderings(currentUser) foreach {events ⇒
        bindings.value.insertAll(0, events)
        EmailManager.send(events)
      }

    }
  }

  @dom
  def emailDom(user: User, events: BindingSeq[Binding[Node]]): Binding[Node] = {
    val linkParams = Router.changeHub(user, Router.defaultRoutingParams)
    val hash = Router.toHash(linkParams)

    <div id="viewport">
      <p>
        <a target="_blank" class="plenty-link" id="link" href={baseUrl + "/" + hash}>View feed on Plenty</a>
      </p>
      {for(b <- events) yield b.bind}
    </div>
  }
}
