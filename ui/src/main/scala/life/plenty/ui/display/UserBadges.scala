package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.hub.User
import life.plenty.model.hub.definition.Hub
import life.plenty.ui.model.{DisplayModule, SimpleDisplayModule, jdenticon}
import org.scalajs.dom.Node
import life.plenty.ui.display.utils.Helpers._
import org.scalajs.dom.html.{Canvas, Div}

class FullUserBadge(override val hub: User) extends DisplayModule[User] {
  override def update(): Unit = Unit

  override protected def generateHtml(): Binding[Node] = FullUserBadge.html(hub)
}

object FullUserBadge extends SimpleDisplayModule[User]{
  @dom
  override def html(hub: User): Binding[Node] = {
    println("DISPLAYING USER BADGE")
    <div class="user-badge">
      {Identicon.generate(hub).bind}
      {hub.getName.dom.bind}
      <script>
        jdenticon.update('.{Identicon.iconId(hub)}')
      </script>
    </div>
  }
  override def fits(hub: Any): Boolean = hub.isInstanceOf[User]
}

object Identicon {
  def iconId(u: User) = "icon" + u.id.replace("@", "").replace(".", "")

  @dom
  def generate(u: User): Binding[Node] = {
    val c: Canvas = <canvas data:data-jdenticon-value={u.id} width={32} height={32} class={iconId(u)}>
      avatar
    </canvas>
    c
  }
}
