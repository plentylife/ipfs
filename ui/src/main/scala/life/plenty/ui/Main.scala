package life.plenty.ui

import com.thoughtworks.binding.{Binding, dom}
import life.plenty.data.{OctopusReader, Main ⇒ dataMain}
import life.plenty.model.connection.{Body, Parent, Title}
import life.plenty.model.octopi.GreatQuestions.Who
import life.plenty.model.octopi._
import life.plenty.model.{initialize ⇒ mInit}
import life.plenty.ui.display.Help
import life.plenty.ui.model.UiContext.getCreator
import life.plenty.ui.model.{DisplayModel, UiContext}
import org.scalajs.dom.raw.Node
import org.scalajs.dom.{Event, document}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scalaz.std.list._
@JSExportTopLevel("Main")
object Main {

  @JSExport
  def main(): Unit = {
    println("Entry point")

    dataMain.main()
    //    Router.initialize
    mInit()
    initialize()


    val ts = new BasicSpace()
    ts.asNew(Title("test"), getCreator)
    val who = new Who()
    who.asNew(Parent(ts), getCreator)
    val q = new BasicQuestion()
    q.asNew(Parent(who), Title("is asking these"), getCreator)
    val a = new BasicAnswer()
    a.asNew(Parent(q), Body("I am asking these"), getCreator)



    println(s"ui loading ${ts.id}")
    OctopusReader.read(ts.id) foreach { spaceOpt ⇒
      spaceOpt foreach { s ⇒ UiContext.startingSpace = s.asInstanceOf[Space] }

      dom.render(document.body, mainSection(spaceOpt))
    }
  }

  @dom
  def mainSection(space: Option[Octopus]): Binding[Node] = {
    <div id="viewport" onclick={e: Event ⇒ Help.triggerClose()}>
      {Help.display().bind}{if (space.nonEmpty) DisplayModel.display(space.get).bind else <span>nothing to show</span>}
    </div>
  }

}
