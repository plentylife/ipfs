package life.plenty.ui.model

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.Property
import org.scalajs.dom.Node

object Helpers {

  //  implicit

  implicit class BindableProperty[T](property: Property[T])(implicit parser: T ⇒ String) {
    val inner = Var(property.getSafe)

    property.registerUpdater(() ⇒ {
      println("Bindable property updated")
      println(property.getSafe)
      inner.value_=(property.getSafe)
    })

    @dom
    //    def dom(parser: (T) ⇒ String = {x ⇒ x.toString}): Binding[Node] = {
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
