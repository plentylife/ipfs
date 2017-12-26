package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.PredefinedGraph._
import life.plenty.model.{ConnectionSet, Data, Node}

object GreatQuestionDisplay extends Renderer {
  private val titleToNode = Map[Node, String](why → "Why", who → "Who is")
  override def canDisplay(node: Node, connectionSet: ConnectionSet) = ???
  @dom
  override def display(node: Node, connectionSet: ConnectionSet): Binding[org.scalajs.dom.raw.Node] = {
    val title = Var(titleToNode.getOrElse(node, "Oops... this should't happen"))
    <div class="great-question-outer-box">
      <div class="great-question-title">
        <span>
          {title.bind}
        </span>
        <div class="great-question-create-box">
          <input type="text"/>
        </div>
      </div>
    </div>
  }
  @dom
  private def displayChildQuestions(spaceNode: Data[String], connectionSet: ConnectionSet): Binding[org.scalajs.dom.raw
  .Node] = {
    val questions = 
      <div class="great-question-inner-box">

      </div>
  }
}
