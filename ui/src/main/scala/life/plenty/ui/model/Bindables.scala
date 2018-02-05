package life.plenty.ui.model

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.Octopus
import life.plenty.ui.model.DisplayModel.{ActionDisplay, DisplayModule}
import org.scalajs.dom.Node
import rx.{Ctx, Rx}

object Helpers {

  trait Bindable[T] {
    val rxv: Rx[T]
    implicit val parser: T ⇒ String

    def dom: Binding[Node]

    val inner = Var(rxv.now)

    implicit val ctx: Ctx.Owner = Ctx.Owner.safe()

    rxv.foreach(p ⇒ {
      inner.value_=(p)
    })
  }

  implicit class OptBindableProperty[T](override val rxv: Rx[Option[T]])(implicit _parser: T ⇒ String) extends
    Bindable[Option[T]] {
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

  implicit class BindableProperty[T](override val rxv: Rx[T])(implicit val parser: T ⇒ String) extends Bindable[T] {
    @dom
    def dom: Binding[Node] = {
      <span class={s"${rxv.getClass.getSimpleName}"}>
        {parser(inner.bind)}
      </span>
    }
  }

  class BindableAction[T <: ActionDisplay[_]](val module: Option[T], calledBy: DisplayModule[Octopus]) {
    val active: Var[Boolean] = module.map(_.active) getOrElse Var(false)

    @dom
    private def empty: Binding[Node] = <span class="d-none"></span>

    def dom: Binding[Node] = {
      val opt: Option[Binding[Node]] = module flatMap {_.display(Option(calledBy), Nil)}
      opt getOrElse empty
    }
  }

}
