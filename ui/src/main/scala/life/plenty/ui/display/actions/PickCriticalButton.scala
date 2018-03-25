package life.plenty.ui.display.actions
import com.thoughtworks.binding.Binding.Var
import life.plenty.ui.display.utils.Helpers._
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.actions.{ActionAddConfirmedMarker, ActionToggleCriticalConnection}
import life.plenty.model.connection.Child
import life.plenty.model.hub.{ContainerSpace, Members, Question, Space}
import life.plenty.model.hub.definition.Hub
import life.plenty.model.utils.GraphUtils; import life.plenty.model.utils.GraphExtractorsDEP
import life.plenty.ui.display.{Controller, InlineDisplay, Modal, TreeView}
import life.plenty.ui.display.actions.labeltraits.MenuAction
import life.plenty.ui.display.utils.{BooleanInputVar, InputVarWithCheckbox}
import life.plenty.ui.display.utils.Helpers.BasicBindable
import life.plenty.ui.model.{ComplexModuleOverride, DisplayModule}
import life.plenty.ui.model.DisplayModel.ActionDisplay
import org.scalajs.dom.{Event, Node}
import rx.{Ctx, Obs}


// todo. would need to extend childDisplay to be Rx
class PickCriticalButton(override val hub: Space) extends DisplayModule[Space] with MenuAction {
  override def update(): Unit = Unit
  private lazy val module = hub.getTopModule({case m:ActionToggleCriticalConnection ⇒ m})

  override def doDisplay(): Boolean = module.nonEmpty

  @dom
  override protected def generateHtml(): Binding[Node] = <div class="btn btn-outline-primary"
                                                              onclick={e: Event ⇒ open()}>
    pick critical questions
  </div>

  private def open() = {
    val html = TreeView(spaceController(hub), {
      case Child(s: ContainerSpace) ⇒ s.rx.getAll({case Child(c) if c.isInstanceOf[Space] ⇒ null}).now match {
        case Nil ⇒ None
        case _ ⇒ Option(getController(s, spaceController))
      }
      case Child(q: Question) ⇒ Option(getController(q,questionController))
      }, Option("there are no questions in this space"))

    Modal.giveContentAndOpen(this, displayModal(html))
  }

  private lazy val title = hub.getTitle
  @dom
  private def displayModal(html: Binding[Node]): Binding[Node] = {
    <div>
      <h5>Users trying to join this space will have to answer selected questions first</h5>
      <p>Below is the list of all questions and spaces within `{title.dom.bind}` </p>
      {html.bind}
    </div>
  }

  private val critical = GraphExtractorsDEP.getCritical(hub)
  private var controllers = Map[Hub, Controller]()

  private def getController[T <: Hub](h: T, generator: T ⇒ Controller): Controller = synchronized {
    val existing = controllers.get(h)
    if (existing.nonEmpty) existing.get
    else {
      val newController = generator(h)
      controllers += h → newController
      newController
    }
  }

  private def questionController(h: Space): Controller = {
    new Controller {
      private implicit val ctx: Ctx.Owner = Ctx.Owner.safe()

      val isSelected = critical.map(list ⇒
        list.exists(c ⇒ c.value.id == h.id)
      )
      val inputVar = new BooleanInputVar({isSelected map {i ⇒ Option(i)} : BasicBindable[Option[Boolean]]}.inner)

      isSelected.foreach(s ⇒ {
        println(s"IsSelected for ${h} is $s; $this; \n $hub")
      })
      inputVar.inputRx.foreach {_.foreach {
        s ⇒
        println(s"Input Selected for ${h} is $s")
        module.get.toggle(h)
      }}

      override def onClick(e: Event): Unit = Unit
      override def cssClasses: Var[String] = new BasicBindable(isSelected map {s ⇒ if (s) "critical" else ""})()

      @dom
      override def content: Binding[Node] = {
        val t = new BasicBindable(h.getTitle)().bind
        new InputVarWithCheckbox(inputVar, t getOrElse "").dom.bind
      }
      override def hub: Hub = h
    }
  }

  private def spaceController(s: Space) = new Controller {
    override def onClick(e: Event): Unit = Unit
    override def cssClasses: Var[String] = Var("")
    @dom
    override def content: Binding[Node] = <b>{s.getTitle.dom.bind}</b>
    override def hub: Hub = s
  }
}