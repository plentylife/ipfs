package life.plenty.ui.model

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.utils.Property
import org.scalajs.dom.Node
import rx.Ctx

object Helpers {

  implicit class BindableProperty[T](property: Property[T])(implicit parser: T ⇒ String) {
    val inner = Var(property.getSafe)

    //    property.registerUpdater(() ⇒ {
    //      println(property.getSafe)
    //      inner.value_=(property.getSafe)
    //    })
    implicit val ctx: Ctx.Owner = Ctx.Owner.safe()

    property.getRx.foreach(p ⇒ {
      println("rx property update", p)
      inner.value_=(p)
    })

    @dom
    def dom: Binding[Node] = {
      if (inner.bind.nonEmpty) {
        <span class={s"${property.getClass.getSimpleName}"}>
          {parser(inner.bind.get)}
        </span>
      } else {
        <span></span>
      }
    }
  }

}
