package life.plenty.ui.model

import com.thoughtworks.binding.Binding.{Var, Vars}
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.definition.{Hub, Module}
import life.plenty.ui
import life.plenty.ui.model.DisplayModel.DisplayModule
import org.scalajs.dom.Node
import rx.Ctx

import scala.language.postfixOps
import scalaz.std.list._

object DisplayModel {
  implicit def intToStr(i: Int): String = i.toString


  def display(o: Hub, overrides: List[ModuleOverride], calledBy: DisplayModule[Hub]): Binding[Node] = {
    display(o, overrides, Option(calledBy))
  }

  def display(o: Hub, overrides: List[ModuleOverride] = List(),
              calledBy: Option[DisplayModule[_]] = None): Binding[Node] = {
    o.modules.find {
      case dm: DisplayModule[_] ⇒
        ui.console.trace(s"DisplayModel searching for an available display module: ${dm} ${dm.doDisplay()}")
        dm.doDisplay()
      case _ ⇒ false
    } flatMap {_.asInstanceOf[DisplayModule[_]].display(calledBy, overrides)} getOrElse noDisplay
  }

  @dom
  private def noDisplay: Binding[Node] = <div>This octopus has no display</div>

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

  trait DisplayModule[+T <: Hub] extends Module[T] {
    implicit def its(i: Int): String = intToStr(i)

    implicit val ctx: Ctx.Owner = Ctx.Owner.safe()


    private var _hasRenderedOnce = false
    private var lastCaller: Option[DisplayModule[_]] = None
    private var needsRefresh: Boolean = false

    private var htmlCache: Binding[Node] = _

    protected val cachedOverrides = Vars[ModuleOverride]()

    def display(calledBy: DisplayModule[Hub], overrides: List[ModuleOverride] = List()): Option[Binding[Node]] = {
      this.display(Option(calledBy), overrides)
    }

    def doDisplay(): Boolean = true

    /** for child classes to override*/
    def overrides: List[ModuleOverride] = List()

    def display(calledBy: Option[DisplayModule[_]], overrides: List[ModuleOverride]): Option[Binding[Node]] =
      synchronized {
      overriddenBy(overrides) match {
        case Some(module) ⇒
          ui.console.trace(s"${this} overriden by $module according to ${overrides}")
          module.display(calledBy, overrides)
        case _ ⇒ if (doDisplay()) {
          //          println("displaying ", this, withinOctopus, calledBy)
          cachedOverrides.value.clear()
          cachedOverrides.value.insertAll(0, overrides)
          update()
          needsRefresh = !lastCaller.contains(calledBy.orNull) || calledBy.exists(_.needsRefresh)
          val displayResult = if (!_hasRenderedOnce || needsRefresh) {
            _hasRenderedOnce = true
            val html = generateHtml()
            htmlCache = html
            Option(html)
          } else {
            Option(htmlCache)
          }
          lastCaller = calledBy
          displayResult
        } else None
      }
    }

    def update(): Unit

    def hasRendered = _hasRenderedOnce

    def containerClasses: Set[String] = Set()

    protected def generateHtml(): Binding[Node]

    private def overriddenBy(overrides: List[ModuleOverride]): Option[DisplayModule[_]] =
      overrides.collectFirst {
        case mo: ModuleOverride if mo.creator != this && mo.condition(this) ⇒ mo
      } flatMap {
        case SimpleModuleOverride(_, by, _) ⇒ Option(by)
        case ComplexModuleOverride(_, finder, _) ⇒ hub.getTopModule[DisplayModule[Hub]](finder)
          : Option[DisplayModule[Hub]]
      }
  }

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