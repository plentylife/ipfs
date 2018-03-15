package life.plenty.ui.model

import com.thoughtworks.binding.Binding.{Var, Vars}
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.hub.definition.{Hub, Module}
import life.plenty.ui
import org.scalajs.dom.Node
import rx.Ctx
import scalaz.std.list._

object DisplayModel {
  def display(o: Hub, overrides: List[ModuleOverride], calledBy: DisplayModule[Hub]): Binding[Node] = {
    display(o, overrides, Option(calledBy))
  }

  def display(o: Hub, overrides: List[ModuleOverride] = List(),
              calledBy: Option[DisplayModule[_]] = None): Binding[Node] = {
    val excludes = overrides collect {case o: ExclusiveModuleOverride ⇒ o.excluded}
    o.modules.find {
      case dm: DisplayModule[_] ⇒
        ui.console.trace(s"DisplayModel searching for an available display module: ${dm} ${dm.doDisplay()} " +
          s"${excludes map {f ⇒ f(dm)} } $calledBy")
        dm.doDisplay() && {excludes map {f ⇒ f(dm)} forall {_ == false}}
      case _ ⇒ false
    } flatMap {_.asInstanceOf[DisplayModule[_]].display(calledBy, overrides)} getOrElse noDisplay
  }

  @dom
  private def noDisplay: Binding[Node] = <div>This card has no display</div>

  def reRender(o: Hub, moduleSelector: PartialFunction[Module[Hub], DisplayModule[Hub]] = {
    case m: DisplayModule[_] ⇒ m
  }): Unit =
    o.getModules(moduleSelector).foreach(m ⇒ {
      if (m.hasRendered) {
        //        println("re-render of module", m, m.withinOctopus)
        m.update()
      }
    })
  def getSiblingModules(self: DisplayModule[Hub]): List[DisplayModule[Hub]] = self.hub.getModules {
    case m: DisplayModule[_] if m != self ⇒ m
  }

  @dom
  def nospan: Binding[Node] = <span class="d-none"></span>

  //  lazy val nospan: Elem = <span class="d-none"></span>

  /* the main trait */


  trait ActionDisplay[T <: Hub] extends DisplayModule[T] {
    val active = Var(false)
    protected val isEmpty = Var(false)

    protected def setInactive = active.value_=(false)

    def activeDisplay: Binding[Node]

    def inactiveDisplay: Binding[Node]


    @dom
    override protected def generateHtml(): Binding[Node] = <span class={this.getClass.getSimpleName}>
      {if (!isEmpty.bind) {
        if (active.bind) {
          activeDisplay.bind
        } else {
          inactiveDisplay.bind
        }
      } else {
        DisplayModel.nospan.bind
      }}
    </span>
  }

  trait SingleActionModuleDisplay[T <: Hub] extends DisplayModule[T] {
    protected val module: Option[Module[T]]

    protected def presentGenerateHtml(): Binding[Node]

    @dom
    override protected def generateHtml(): Binding[Node] =
      {if (module.nonEmpty) {
        presentGenerateHtml().bind
      } else {
        DisplayModel.nospan.bind
      }}
  }
}