package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.User
import life.plenty.ui.model.DisplayModel.DisplayModule
import org.scalajs.dom.Node
import life.plenty.ui.display.utils.Helpers._
import life.plenty.ui.model.jdenticon
import org.scalajs.dom.html.{Canvas, Div}
class FullUserBadge(override val withinOctopus: User) extends DisplayModule[User] {
  override def update(): Unit = Unit

  @dom
  override protected def generateHtml(): Binding[Node] = {
    withinOctopus.getName.foreach(n ⇒ {
      println(s"&&NAME $n in ${withinOctopus.id} ${withinOctopus.connections.now}")
    })

    <div class="user-badge">
      {Identicon.generate(withinOctopus).bind}
      {withinOctopus.getName.dom.bind}
      <script>
        jdenticon.update("{"#" + Identicon.iconId(withinOctopus)}")
      </script>
    </div>
  }
}

object Identicon {
  def iconId(u: User) = u.id.replace("@", "").replace(".", "") + "icon"

  @dom
  def generate(u: User): Binding[Node] = {
    val c: Canvas = <canvas data:data-jdenticon-value={u.id} width={32} height={32} id={iconId(u)}>
      avatar
    </canvas>
    c
  }
}
