package life.plenty.ui.display

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import life.plenty.model.PredefinedGraph._
import life.plenty.model._
import life.plenty.model.utilities.Traversal._


object SpaceDisplay extends DataNodeRenderer[String] {
  @dom
  def displayData(spaceNode: Data[String], connectionSet: ConnectionSet): Binding[org.scalajs.dom.raw.Node] = {
    val title = Var(spaceNode.data)
    <div>
      <div class="space-title">
        {title.bind}
      </div>{displayChildren(spaceNode, connectionSet).bind}
    </div>
  }

  @dom
  def displayChildren(spaceNode: Data[String], connectionSet: ConnectionSet):
  Binding[org.scalajs.dom.raw.Node] = {
    <div class="space-contents">
      {for (q <- greatQuestionsOrdered) yield {
      GreatQuestionDisplay.display(q, connectionSet).bind
    }}
    </div>
  }

  override def canDisplay(node: Node, connectionSet: ConnectionSet) = {
    val cs = getDirectFromConnections(node, connectionSet.connections)
    val can = cs.exists(c ⇒ c.conType == USER_CREATED_INSTANCE_OF && c.to == space)
    if (can) Option(RenderWithNoPriority) else None
  }


}
