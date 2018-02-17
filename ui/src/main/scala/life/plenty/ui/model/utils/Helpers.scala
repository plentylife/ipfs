package life.plenty.ui.model.utils

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.definition.Hub
import life.plenty.ui.model.DisplayModel.{ActionDisplay, DisplayModule}
import life.plenty.ui.model.{DisplayModel, ModuleOverride, UiContext}
import org.scalajs.dom.Node
import org.scalajs.dom.html.Input
import org.scalajs.dom.raw.Event
import rx.{Ctx, Rx}

import scalaz.std.list._
import scalaz.std.option._

object Helpers {

  @dom
  def strIntoParagraphs(str: String): Binding[Node] = {
    val split = str.split("\n").toList
    <span>
      {for (line ← split) yield {
      <p>
        {line.trim}
      </p>
    }}
    </span>
  }

  def sameAsUiStarting(h: Hub): Boolean = UiContext.startingSpace.value.exists(_.id == h.id)

  trait Bindable[T] {
    val rxv: Rx[T]

    val inner = Var(rxv.now)

    implicit val ctx: Ctx.Owner = Ctx.Owner.safe()

    rxv.foreach(p ⇒ {
      inner.value_=(p)
    })

    def apply(): Var[T] = inner
  }

  trait BindableDom[T] extends Bindable[T] {
    implicit val parser: T ⇒ String

    def dom: Binding[Node]
  }

  implicit class BasicBindable[T](override val rxv: Rx[T]) extends Bindable[T]

  implicit class OptBindableProperty[T](override val rxv: Rx[Option[T]])(implicit _parser: T ⇒ String) extends
    BindableDom[Option[T]] {
    @dom
    def dom: Binding[Node] = {
      if (inner.bind.nonEmpty) {
        <span class={s"${rxv.getClass.getSimpleName}"}>
          {inner.bind: String}
        </span>
      } else {
        <span></span>
      }
    }

    override implicit val parser: Option[T] ⇒ String = opt ⇒ opt.map(_parser).getOrElse("")
  }

  class OptBindableHub(override val rxv: Rx[Option[Hub]], caller: DisplayModule[Hub],
                       overrides: List[ModuleOverride] = Nil)    extends    Bindable[Option[Hub]] {
    @dom
    def dom: Binding[Node] = this().bind.map {h ⇒ DisplayModel.display(h, overrides, Option(caller)).bind} getOrElse {
      DisplayModel.nospan.bind
    }
  }

  implicit class BindableProperty[T](override val rxv: Rx[T])(implicit val parser: T ⇒ String) extends BindableDom[T] {
    @dom
    def dom: Binding[Node] = {
      <span class={s"${rxv.getClass.getSimpleName}"}>
        {parser(inner.bind)}
      </span>
    }
  }

  class BindableAction[T <: ActionDisplay[_]](override val module: Option[T], calledBy: DisplayModule[Hub]) extends
  BindableModule[T](module, calledBy) {
    val active: Var[Boolean] = module.map(_.active) getOrElse Var(false)
  }


  class BindableModule[T <: DisplayModule[_]](val module: Option[T], calledBy: DisplayModule[Hub]) {
    def dom: Binding[Node] = {
      val opt: Option[Binding[Node]] = module flatMap {_.display(Option(calledBy), Nil)}
      opt getOrElse DisplayModel.nospan
    }
  }
}
