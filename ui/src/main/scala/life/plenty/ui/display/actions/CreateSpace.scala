package life.plenty.ui.display.actions

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.connection.{Body, Parent, Title}
import life.plenty.model.octopi.{ContainerSpace, Space}
import life.plenty.ui.display.Login._
import life.plenty.ui.display.Modal
import org.scalajs.dom.{Event, Node}
import org.scalajs.dom.html.Input
import life.plenty.ui.model.Helpers._
import life.plenty.ui.model.{DisplayModel, Router}

import scalaz.std.option._

object CreateSpace {
  private val title = new InputVar
  private val description = new InputVar
  private var parentSpace: Var[Option[Space]] = Var(None)


  private def onSubmit(event: Event): Unit = {
    event.preventDefault()

    for (t ← title.get; d ← description.get) {
      val space = new ContainerSpace
      space.asNew(Title(t), Body(d))
      parentSpace.value foreach {ps ⇒ space.addConnection(Parent(ps))}

      println(space.sc.all)

      Router.navigateToOctopus(space)
      Modal.close()
      title.reset
      description.reset
      parentSpace.value_=(None)
    }
  }

  def openInModal(ps: Space = null): Unit = {
    parentSpace.value_=(Option(ps))
    Modal.setContentAndOpen(createSpaceDisplay())
  }

  @dom
  def createSpaceDisplay(): Binding[Node] = {
      <form class="d-flex flex-column align-items-center create-space-form" onsubmit={onSubmit _}>
        {
        parentSpace.bind.map(s => {
          <div class="parent-space">
            Parent space will be `{s.getTitle.dom.bind}`
          </div>
        }) getOrElse DisplayModel.nospan.bind
        }
      <div class="mt-2">
          {if (title.isEmpty.bind) {
          <div class="bg-danger text-white">
            Title can't be empty
          </div>
        } else {
          <span style="display:none"></span>
        }}<label for="title">Space title</label> <br/>
          <input name="title" type="text" oninput={title.input _}/>
        </div>
        <div class="mt-2">
          {if (description.isEmpty.bind) {
          <div class="bg-danger text-white">
            Description can't be empty
          </div>
        } else {
          <span style="display:none"></span>
        }}<label for="description">Description</label> <br/>
          <textarea name="description" oninput={description.input _}></textarea>
          <br/>
        </div>
        <input type="submit" class="btn btn-primary mt-2" value="Create Space"/>
      </form>
  }
}
