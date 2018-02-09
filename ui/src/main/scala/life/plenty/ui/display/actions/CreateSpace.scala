package life.plenty.ui.display.actions

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.connection.{Body, Title}
import life.plenty.model.octopi.BasicSpace
import life.plenty.ui.display.Login._
import life.plenty.ui.display.Modal
import org.scalajs.dom.{Event, Node}
import org.scalajs.dom.html.Input
import life.plenty.ui.model.Helpers._

//<div class="d-flex flex-column align-items-center">
//
//</div>

object CreateSpace {
  private val title = new InputVar
  private val description = new InputVar


  private def onSubmit(event: Event): Unit = {
    event.preventDefault()

    for (t ← title.get; d ← description.get) {
      val space = new BasicSpace
      space.asNew(Title(t), Body(d))

      println(space.sc.all)

      title.reset
      description.reset
    }
  }

  def openInModal(): Unit = Modal.setContentAndOpen(createSpaceDisplay())

  @dom
  def createSpaceDisplay(): Binding[Node] = {
      <form class="d-flex flex-column align-items-center" onsubmit={onSubmit _}>
      <div>
          {if (title.isEmpty.bind) {
          <div class="bg-danger text-white">
            Title can't be empty
          </div>
        } else {
          <span style="display:none"></span>
        }}<label for="title">Space title</label> <br/>
          <input name="title" type="text" oninput={title.input(_)}/>
        </div>
        <div class="mt-2">
          {if (description.isEmpty.bind) {
          <div class="bg-danger text-white">
            Description can't be empty
          </div>
        } else {
          <span style="display:none"></span>
        }}<label for="description">Description</label> <br/>
          <textarea name="description" oninput={description.input(_)}>
            </textarea>
          <br/>
        </div>
        <input type="submit" class="btn btn-primary mt-2" value="Create Space"/>
      </form>
  }
}
