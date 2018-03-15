package life.plenty.ui.display.actions

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.hub.Space
import life.plenty.ui.display.utils.{BooleanInputVar, InputVarWithCheckbox}
import life.plenty.ui.display.utils.Helpers.BasicBindable
import life.plenty.ui.model.DisplayModule
import org.scalajs.dom.{Event, Node}

//class SelectCritical(override val hub: Space) extends DisplayModule[Space] {
//  override def update(): Unit = Unit
//
//  val t = new BasicBindable(h.getTitle)().bind
//
//  val isSelected = critical.map(list ⇒
//    list.exists(c ⇒ c.value.id == h.id)
//  )
//  val inputVar = new BooleanInputVar({isSelected map {i ⇒ Option(i)} : BasicBindable[Option[Boolean]]}.inner)
//
//  isSelected.foreach(s ⇒ {
//    println(s"IsSelected for ${h} is $s; $this; \n $hub")
//  })
//  inputVar.inputRx.foreach {_.foreach {
//    s ⇒
//      println(s"Input Selected for ${h} is $s")
//      module.get.toggle(h)
//  }}
//
//  override def onClick(e: Event): Unit = Unit
//  override def cssClasses: Var[String] = new BasicBindable(isSelected map {s ⇒ if (s) "critical" else ""})()
//
//  @dom
//  override def content: Binding[Node] = {
//    new InputVarWithCheckbox(inputVar, t getOrElse "").dom.bind
//  }
//
//  override protected def generateHtml(): Binding[Node] = ???
//}
