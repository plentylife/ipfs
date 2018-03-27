package life.plenty.ui.emailNotification

import com.thoughtworks.binding.Binding.{BindingSeq, Vars}
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.data.{DbReader, ShareDB, Main ⇒ dataMain}
import life.plenty.model.hub.User
import life.plenty.model.utils.GraphEx._
import life.plenty.model.{console ⇒ modelConsole, initialize ⇒ mInit}
import life.plenty.ui.model._
import life.plenty.{data, ui}
import org.scalajs.dom.document
import org.scalajs.dom.raw.Node
import scalaz.std.list._

import scala.concurrent.ExecutionContext.Implicits.{global ⇒ exg}
import scala.scalajs.js
import scala.scalajs.js.Dynamic.global
import scala.scalajs.js.Promise
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("Email")
object EmailMain {

  private def emailNext = global.nextUserToEmail
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
    val uEmail = getEmail(currentUser)
    for (e ← uEmail) {
      ready(e.get)
    }
  }

  @JSExport
  //"yv5_7GVnoTxA-DZ_DNvOQ6NhaRJAN_wbfoXpRfdEy8SSAnoMGooxYg"
  def next(): Unit = {
    emailNext().asInstanceOf[Promise[String]].toFuture.foreach {id ⇒
      println(s"Received id $id")
      DbReader.read(id.toString) foreach { hub ⇒
        currentUser = hub.asInstanceOf[User]

        val bindings = Vars[Binding[Node]]()
        dom.render(document.getElementById("body-container"), emailDom(hub.asInstanceOf[User], bindings))

        EmailBuilder.renderings(currentUser) foreach { events ⇒
          bindings.value.insertAll(0, events)
          EmailManager.send(events)
        }

      }
    }

  }

  import life.plenty.ui.display.utils.FutureDom._

  @dom
  def emailDom(user: User, events: BindingSeq[Binding[Node]]): Binding[Node] = {
    val linkParams = Router.changeHub(user, Router.defaultRoutingParams)
    val hash = Router.toHash(linkParams)

    <div id="viewport">

      <p>
        {propertyDom(getName(currentUser)).bind}
        ,</p>

      <p>
        This is a summary of what has happened on Plenty since your last login.
        You can always email us back at this address if you have any questions or comments.
      </p>

      <p>
        <a target="_blank" class="plenty-link" id="link" href={baseUrl + "/" + hash}>View feed on Plenty</a>
      </p>{for (b <- events) yield b.bind}
    </div>
  }
}
