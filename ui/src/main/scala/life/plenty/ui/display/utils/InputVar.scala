package life.plenty.ui.display.utils

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.Node
import org.scalajs.dom.html.Input
import org.scalajs.dom.raw.Event

import scala.util.Try

trait InputVar[T] {
  val innerVar: Var[Option[T]]
  val extractor: Event ⇒ Option[T]

  val isEmpty = Var(false)

  def input(e: Event): Unit = {
    innerVar.value_=(extractor(e))
    check()
  }

  def check(): Unit = isEmpty.value_=(innerVar.value.isEmpty)

  def get: Option[T] = {
    val v = innerVar.value
    check()
    if (isEmpty.value) None else v
  }

  def reset(): Unit = innerVar.value_=(None)
}

class StringInputVar(override val innerVar: Var[Option[String]] = Var(None)) extends InputVar[String] {
  override val extractor: Event ⇒ Option[String] = e ⇒ {
    val v = e.target.asInstanceOf[Input].value.trim
    if (v.isEmpty) None else Option(v)
  }
}

class TransactionalAmountVar(override val innerVar: Var[Option[Int]] = Var(None)) extends InputVar[Int] {
  override val extractor: Event ⇒ Option[Int] = e ⇒ {
    val v = e.target.asInstanceOf[Input].value.trim
    if (v.isEmpty) None else {
      Try {v.toInt}.toOption flatMap {i ⇒ if (i > 0) Option(i) else None}
    }
  }
}

class BooleanInputVar(override val innerVar: Var[Option[Boolean]] = Var(None)) extends InputVar[Boolean] {
  override val extractor: Event ⇒ Option[Boolean] = e ⇒ {
    val v = e.target.asInstanceOf[Input].checked
    println(s"extracted $v")
    Option(v)
  }
}

class InputVarWithDisplay(inputVar: InputVar[_], label: String, cssClasses: String = "", errorMsg: String = "") {

  @dom
  def dom: Binding[Node] = {
    <div class={cssClasses + " input-var"}>
      {if (inputVar.isEmpty.bind) {
      <div class="bg-danger text-white">
        {if (errorMsg.isEmpty) s"$label can't be empty" else errorMsg}
      </div>
    } else {
      <span style="display:none"></span>
    }}<label for={this.toString}>{label}</label> <br/>
      {inputElem.bind}
    </div>
  }

  @dom
  protected def inputElem: Binding[Node] = <input name={this.toString} type="text" oninput={inputVar.input _}/>
}

class InputVarWithTextarea(inputVar: StringInputVar, label: String) extends InputVarWithDisplay(inputVar, label) {
  @dom
  override protected def inputElem = <textarea name={this.toString} oninput={inputVar.input _}></textarea>
}

class InputVarWithPassword(inputVar: StringInputVar, label: String) extends InputVarWithDisplay(inputVar, label) {
  @dom
  override protected def inputElem = <input name={this.toString} type="password" oninput={inputVar.input _}/>
}

class InputVarWithCheckbox(inputVar: BooleanInputVar, label: String) extends InputVarWithDisplay(inputVar, label) {
  @dom
  override def dom: Binding[Node] = {
    <div class="form-check">
      <input class="form-check-input" type="checkbox" value="" id={this.toString} onchange={inputVar.input _}/>
      <label class="form-check-label" for={this.toString}>
        {label}
      </label>
    </div>
  }

  @dom
  override protected def inputElem = ???
}