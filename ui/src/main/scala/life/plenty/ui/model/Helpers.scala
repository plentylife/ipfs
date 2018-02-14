package life.plenty.ui.model

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.definition.Hub
import life.plenty.ui.display.actions.CreateSpace.title
import life.plenty.ui.model.DisplayModel.{ActionDisplay, DisplayModule}
import org.scalajs.dom.Node
import org.scalajs.dom.html.Input
import org.scalajs.dom.raw.Event
import rx.{Ctx, Rx}

import scala.xml.Elem
import scalaz.std.list._

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

  class InputVar(innerVar: Var[String] = Var("")) {
    val isEmpty = Var(false)

    def input(e: Event): Unit = {
      val v = e.target.asInstanceOf[Input].value.trim
      isEmpty.value_=(v.isEmpty)
      innerVar.value_=(v)
    }

    def check(): Unit = isEmpty.value_=(innerVar.value.isEmpty)

    def get: Option[String] = {
      val v = innerVar.value
      check()
      if (isEmpty.value) None else Option(v)
    }

    def reset(): Unit = innerVar.value_=("")
  }

  class InputVarWithDisplay(inputVar: InputVar, label: String, cssClasses: String = "") {

    @dom
    def dom: Binding[Node] = {
      <div class={cssClasses + " input-var"}>
        {if (inputVar.isEmpty.bind) {
        <div class="bg-danger text-white">
          {label} can't be empty
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

  class InputVarWithTextarea(inputVar: InputVar, label: String) extends InputVarWithDisplay(inputVar, label) {
    @dom
    override protected def inputElem = <textarea name={this.toString} oninput={inputVar.input _}></textarea>
  }
}
