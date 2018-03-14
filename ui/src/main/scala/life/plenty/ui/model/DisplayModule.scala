package life.plenty.ui.model

import com.thoughtworks.binding.Binding
import life.plenty.model.octopi.definition.{Hub, Module}
import org.scalajs.dom.Node
import rx.Ctx
import com.thoughtworks.binding.Binding.{Var, Vars}
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.definition.{Hub, Module}
import life.plenty.ui
import org.scalajs.dom.Node
import rx.Ctx
import scalaz.std.list._
import life.plenty.ui.display.utils.Helpers._

trait DisplayModule[+T <: Hub] extends Module[T] {
//  implicit def its(i: Int): String = intToStr(i)

  implicit val ctx: Ctx.Owner = Ctx.Owner.safe()


  private var _hasRenderedOnce = false
  protected var lastCaller: Option[DisplayModule[_]] = None
  private var needsRefresh: Boolean = false

  private var htmlCache: Binding[Node] = _

  protected val cachedOverrides = Vars[ModuleOverride]()

  def display(calledBy: DisplayModule[Hub], overrides: List[ModuleOverride] = List()): Option[Binding[Node]] = {
    this.display(Option(calledBy), overrides)
  }

  def doDisplay(): Boolean = true

  /** for child classes to override */
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
      case mo: ConditionalModuleOverride if mo.creator != this && mo.condition(this) ⇒ mo
    } flatMap {
      case SimpleModuleOverride(_, by, _) ⇒ Option(by)
      case ComplexModuleOverride(_, finder, _) ⇒
        hub.getTopModule[DisplayModule[Hub]](finder): Option[DisplayModule[Hub]]
    }
}