package life.plenty.ui

import com.thoughtworks.binding.Binding
import life.plenty.model.PredefinedGraph._
import life.plenty.model.{ConnectionSet, Data, Node}

package object display {

  val rendererDirectory = Set(
    SpaceDisplay
  )

  val greatQuestionsOrdered = List(who, why)

  trait Renderer {
    def canDisplay(node: Node, connectionSet: ConnectionSet): Option[RendersWithPriority]

    /** @note Context of the display can be set by pre-filtering the connections */
    def display(node: Node, connectionSet: ConnectionSet): Binding[org.scalajs.dom.raw.Node]
  }

  trait DataNodeRenderer[T] extends Renderer {
    override def display(node: Node, connectionSet: ConnectionSet): Binding[org.scalajs.dom.raw.Node] = {
      node match {
        case n: Data[T] ⇒ displayData(n, connectionSet)
        case _ ⇒ null
      }
    }

    def displayData(node: Data[T], connectionSet: ConnectionSet): Binding[org.scalajs.dom.raw.Node]
  }

  trait RendersWithPriority {
    val over: Set[Renderer]
  }

  object RenderWithNoPriority extends RendersWithPriority {
    val over = Set()
  }
}
