package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.actions.ActionAddDescription
import life.plenty.model.connection.Body
import life.plenty.model.octopi.Space
import life.plenty.ui.model.DisplayModel.DisplayModule
import life.plenty.ui.model.{DisplayModel, UiContext}
import org.scalajs.dom.html.Input
import org.scalajs.dom.{Event, Node}
import rx.Obs

class DescriptionDisplay(override val withinOctopus: Space) extends DisplayModule[Space] {
  private var obs: Obs = null

  override def update(): Unit = if (obs == null) {
    obs = descriptionRx.foreach(d ⇒ {
      description.value_=(d.getOrElse(""))
    })
  }

  override def doDisplay(): Boolean = UiContext.startingSpace.value.get.id == withinOctopus.id


  private lazy val description: Var[String] = Var("")
  private lazy val descriptionRx = withinOctopus.rx.get({ case Body(d) ⇒ d })
  private lazy val action = withinOctopus.getTopModule({ case m: ActionAddDescription ⇒ m })

  private lazy val editorOpen = Var(false)
  private lazy val text = Var("")

  @dom
  override protected def generateHtml(): Binding[Node] = {
    <div class="card d-inline-flex mt-1 mr-1 flex-row">

      <div class="card-body">
        <h6 class="card-title">Description</h6>
        <p class="card-text">
          {if (!editorOpen.bind) {
          <span>
            {descriptionView.bind}
          </span>
        }
        else {
          <textarea oninput={e: Event =>
            val v = e.target.asInstanceOf[Input].value
            text.value_=(v)}></textarea>
        }}
        </p>
      </div>{if (action.nonEmpty) {
      <div class="card-controls-bottom d-inline-flex">
        <button class="btn btn-primary" onclick={e: Event =>
          if (editorOpen.value) {
            action.get.add(text.value)
            text.value_=("")
          }
          editorOpen.value_=(!editorOpen.value)}>
          {buttonTitle.bind}
        </button>
      </div>
    } else DisplayModel.nospan.bind}

    </div>
  }

  @dom
  private def buttonTitle: Binding[String] = if (editorOpen.bind) "Post" else "Add description"

  @dom
  private def descriptionView: Binding[String] = if (description.bind.isEmpty) "no description yet..." else
    description.bind

}
