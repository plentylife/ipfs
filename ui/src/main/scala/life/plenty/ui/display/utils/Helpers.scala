package life.plenty.ui.display.utils

import com.thoughtworks.binding.Binding.{Var, Vars}
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.octopi.definition.Hub
import life.plenty.ui.model.DisplayModel.ActionDisplay
import life.plenty.ui.model.{DisplayModel, DisplayModule, ModuleOverride, UiContext}
import org.scalajs.dom.Node
import rx.{Ctx, Rx}
import scalaz.std.list._
import scalaz.std.option._

import scala.language.implicitConversions

object Helpers {
  implicit def intToStr(i: Int): String = i.toString

  implicit def rxOptString(opt: Rx[Option[String]])(implicit ctx: Ctx.Owner): Rx[String] = opt map {_ getOrElse ""}

  @dom
  def strIntoParagraphs(str: String): Binding[Node] = {
    val split = str.split("\n").toList.filter(_.nonEmpty)
    <span>
      {for (line ← split) yield {
      <p>
        {line.trim}
      </p>
    }}
    </span>
  }

  def sameAsUiPointer(h: Hub): Boolean = UiContext.pointer.value.exists(_.id == h.id)

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

  // todo use this for children
  implicit class ListBindable[T](val rxv: Rx[List[T]]) {
    val inner = Vars[T]()

    implicit val ctx: Ctx.Owner = Ctx.Owner.safe()

    rxv.foreach(l ⇒ {
//      inner.value.clear()
//      inner.value.insertAll(0, l)
      val current = inner.value
      var i = 0
      current zip l foreach {z ⇒
        val (o, n) = z
        if (o != n) current.update(i, n)
        i += 1
      }
      if (current.size > l.size) {
        current.remove(l.size, current.size - l.size)
      } else if (l.size > current.size) {
        current.insertAll(current.size, l.slice(current.size, l.size))
      }

    })

    def apply(): Vars[T] = inner

//    def mapRx[R](f: T ⇒ R) = {
//      val newR = rxv.map(list ⇒ list.map(f))
//      new ListBindable[]()
//    }
  }

  implicit class OptBindableProperty[T](override val rxv: Rx[Option[T]])(implicit _parser: T ⇒ String) extends
    BindableDom[Option[T]] {
    @dom
    def innerHtml: Binding[Node] = {
      <span class={s"${rxv.getClass.getSimpleName}"}>
        {inner.bind}
      </span>
    }

    @dom
    def dom: Binding[Node] = {
      if (inner.bind.nonEmpty) {
        innerHtml.bind
      } else {
        <span></span>
      }
    }

    override implicit val parser: Option[T] ⇒ String = opt ⇒ opt.map(_parser).getOrElse("")
  }

  class OptBindableHtmlProperty[T](override val rxv: Rx[Option[T]], htmlParser: String ⇒ Binding[Node])
                                  (implicit _parser: T ⇒ String) extends OptBindableProperty[T](rxv) {

    @dom
    override def innerHtml: Binding[Node] = htmlParser(inner.bind).bind
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

  class BindableHtmlProperty[T](override val rxv: Rx[T], htmlParser: String ⇒ Binding[Node])
                               (implicit val parser: T ⇒ String) extends BindableDom[T] {
    @dom
    def dom: Binding[Node] = htmlParser(parser(inner.bind)).bind
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
