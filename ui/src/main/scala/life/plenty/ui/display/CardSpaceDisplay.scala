package life.plenty.ui.display

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.{Space, User}
import life.plenty.ui.display.actions.SpaceActionsBar
import life.plenty.ui.display.meta.{LayoutModule, NoDisplay}
import life.plenty.ui.model.{DisplayModel, Helpers, UiContext}
import life.plenty.ui.model.DisplayModel.{DisplayModule, ModuleOverride}
import org.scalajs.dom.Node
import rx.Rx

class CardSpaceDisplay (override val withinOctopus: Space) extends LayoutModule[Space] {
  override def doDisplay() = !Helpers.sameAsUiStarting(withinOctopus)

//  override def overrides: List[DisplayModel.ModuleOverride] = ModuleOverride(
//    this, new NoDisplay(withinOctopus), {h ⇒ h.isInstanceOf[TopSpaceActions] && h.withinOctopus != withinOctopus}
//  ) :: super.overrides

  @dom
  override protected def generateHtml(): Binding[Node] = {
    val cos: Seq[ModuleOverride] = this.cachedOverrides.bind
    implicit val os = cos.toList ::: siblingOverrides

    <div class="card d-inline-flex mt-1 mr-1 flex-column space-card" id={withinOctopus.id}>
      <h3 class="card-title">title</h3>
      <h6 class="card-subtitle mb-2 text-muted">
        descr
      </h6>
      <div class="card-body">

        {displayModules(siblingModules.withFilter(_.isInstanceOf[SpaceActionsBar]), "card-space-menu").bind}

      space card
        </div>
    </div>
  }
}
