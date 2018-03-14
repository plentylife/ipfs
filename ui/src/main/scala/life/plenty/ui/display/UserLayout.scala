package life.plenty.ui.display

import com.thoughtworks.binding.Binding.{BindingSeq, Var}
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi._
import life.plenty.model.octopi.definition.Hub
import life.plenty.model.utils.GraphUtils
import life.plenty.ui.display.actions.SpaceActionsBar
import life.plenty.ui.display.meta.LayoutModule
import life.plenty.ui.display.utils.Helpers._
import life.plenty.ui.model.{DisplayModel, ModuleOverride, UiContext}
import org.scalajs.dom.raw.Node
import scalaz.std.list._
import scalaz.std.option._


class UserLayout(override val hub: User) extends LayoutModule[User] {

  override def doDisplay(): Boolean = true

  def getMemberships(cs: BindingSeq[Hub]): BindingSeq[Hub]#WithFilter = cs.withFilter({
    case m: Members ⇒ true
    case _ ⇒ false
  })

  protected val additionalCss = Var("")

  @dom
  override protected def generateHtml(): Binding[Node] = {
    val cos: Seq[ModuleOverride] = this.cachedOverrides.bind
    implicit val os = cos.toList ::: siblingOverrides ::: overrides
    val titleClasses = "title ml-2 "

    <div class={"top-space-layout " + additionalCss.bind}>

      {val menuBar = siblingModules.withFilter(_.isInstanceOf[MenuBar])
    for (m <- menuBar) yield m.display(this, os) map {_.bind} getOrElse DisplayModel.nospan.bind}


      <span class="span-separator top-space-layout-content">

      <div class="layout-header">
        <h3 class={titleClasses}>
          {GraphUtils.getName(hub).dom.bind}
        </h3>
      </div>

      <div class="user-feed">
        {for (s <- sections) yield s.bind}
      </div>
    </span>
    </div>
  }

  protected def sections(implicit overrides: List[ModuleOverride]): List[Binding[Node]] =
    displayHubs(getMemberships(children), "administrative section") :: Nil
}