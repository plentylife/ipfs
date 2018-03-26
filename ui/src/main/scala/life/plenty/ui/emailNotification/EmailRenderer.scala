package life.plenty.ui.emailNotification

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.data.{DbReader, DbReaderModule, ShareDB, Main ⇒ dataMain}
import life.plenty.model.hub.User
import life.plenty.model.hub.definition.Hub
import life.plenty.model.{console ⇒ modelConsole, initialize ⇒ mInit}
import life.plenty.ui.display.UserLayout
import life.plenty.ui.model._
import life.plenty.{data, ui}
import org.scalajs.dom.document
import org.scalajs.dom.raw.Node
import rx.Ctx
import scalaz.std.list._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("Email")
object EmailRenderer {

  @JSExport
  def render(db: ShareDB): Unit = {
    println("Creating email")
    data.console.active = false
    ui.console.active = false
    modelConsole.active = false

    // has to be first because it sets the hasher function
    dataMain.main(db)
    mInit()
    ui.initialize()

    renderUser("yv5_7GVnoTxA-DZ_DNvOQ6NhaRJAN_wbfoXpRfdEy8SSAnoMGooxYg")
  }

  def renderUser(id: String): Future[Unit] = {
    val p = Promise[Unit]()

    DbReader.read(id) foreach { hub ⇒
      dom.render(document.getElementById("body-container"), email(hub.asInstanceOf[User]))
    }

    p.future
  }

  @dom
  def email(user: User): Binding[Node] = {
    <div id="viewport">
      {UserLayout.html(user).bind}
    </div>
  }
}
