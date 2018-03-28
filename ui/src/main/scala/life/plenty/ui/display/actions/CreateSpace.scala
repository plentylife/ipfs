package life.plenty.ui.display.actions

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.connection.{Body, DataHub, Parent, Title}
import life.plenty.model.hub.{ContainerSpace, Space}
import life.plenty.ui
import life.plenty.ui.display.Login._
import life.plenty.ui.display.Modal
import org.scalajs.dom.{Event, Node}
import org.scalajs.dom.html.Input
import life.plenty.ui.display.utils.Helpers._
import life.plenty.ui.display.utils.StringInputVar
import life.plenty.ui.model.{DisplayModel, Router}

import scalaz.std.option._
import scala.concurrent.ExecutionContext.Implicits.global
object CreateSpace {
  private val title = new StringInputVar
  private val description = new StringInputVar
  private var parentSpace: Var[Option[Space]] = Var(None)

  private def onSubmit(event: Event): Unit = {
    event.preventDefault()
    title.check; description.check

    for (t ← title.get; d ← description.get) {
      val space = new ContainerSpace
      var params: List[DataHub[_]] = Title(t) :: Body(d) :: Nil
      parentSpace.value map {Parent(_)} foreach {params +:= _}
      ui.console.trace(s"Creating space with params $params `$t` `$d`")
      // fixme. there should be an error message on fail
      space.asNew(params:_*) foreach {_ ⇒
        println(space.sc.all)

        Router.navigateToHub(space)
        Modal.close()
        title.reset
        description.reset
        parentSpace.value_=(None)
      }
    }
  }

  def openInModal(ps: Space = null): Unit = {
    parentSpace.value_=(Option(ps))
    Modal.giveContentAndOpen(this, createSpaceDisplay())
  }

  @dom
  def createSpaceDisplay(): Binding[Node] = {
      <form class="d-flex flex-column align-items-center create-space-form" onsubmit={onSubmit _}>
        <div class="help">
          {parentSpace.bind match {
          case Some(p) => <p>This space will appear as sub-space of <span class="parent-space">
              {p.getTitle.dom.bind}</span>. Please give it a title and a description to help other users understand what
            this space is for.
          </p>
          case _ => <div>
            <p>What kind of <b>group</b> are you starting? What are you trying to <b>organize</b>?</p>
            <p>You are creating a space in which everyone will be able to ask questions, vote on proposals and track
              contributions</p>
            <p>Give it a title and a description to help others orient when they join</p>
          </div>
        }
          }
        </div>
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
