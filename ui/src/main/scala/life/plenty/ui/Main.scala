package life.plenty.ui

import com.thoughtworks.binding.Binding.{Var, Vars}
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.html.Table
import org.scalajs.dom.document
import org.scalajs.dom.raw.{HTMLInputElement, Node}

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel, ScalaJSDefined}


//@ScalaJSDefined // required for Scala.js 0.6.x, unless using -P:scalajs:sjsDefinedByDefault
@JSExportTopLevel("Main")
object Main {
//class Main extends js.Object {
//
  @JSExport
  def main(): Unit = {
    dom.render(document.body, mainSection)
  }

  case class Contact(name: Var[String], email: Var[String])

  val data = Vars.empty[Contact]

  @dom def mainSection: Binding[Node] = {
    <section class="main">
      <input type="checkbox" class="toggle-all"/>
      <label for="toggle-all">Mark all as complete</label>
      <ul class="todo-list"></ul>
    </section> : Node
  }

}
