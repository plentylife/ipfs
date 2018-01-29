package life.plenty.ui.model

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.Node
import rx.{Ctx, Rx}

object Helpers {

  implicit class OptBindableProperty[T](rxv: Rx[Option[T]])(implicit parser: T ⇒ String) {
    val inner = Var(rxv.now)

    implicit val ctx: Ctx.Owner = Ctx.Owner.safe()

    rxv.foreach(p ⇒ {
      inner.value_=(p)
    })

    @dom
    def dom: Binding[Node] = {
      if (inner.bind.nonEmpty) {
        <span class={s"${rxv.getClass.getSimpleName}"}>
          {parser(inner.bind.get)}
        </span>
      } else {
        <span></span>
      }
    }
  }

  implicit class BindableProperty[T](rxv: Rx[T])(implicit parser: T ⇒ String) {
    val inner = Var(rxv.now)

    implicit val ctx: Ctx.Owner = Ctx.Owner.safe()

    rxv.foreach(p ⇒ {
      inner.value_=(p)
    })

    @dom
    def dom: Binding[Node] = {
      <span class={s"${rxv.getClass.getSimpleName}"}>
        {parser(inner.bind)}
      </span>
    }
  }

}
