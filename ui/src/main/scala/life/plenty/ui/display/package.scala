package life.plenty.ui

import com.thoughtworks.binding.Binding
import life.plenty.model.{Connection, Data, Node}

package object display {

  val rendererDirectory = Set(
    SpaceDisplay
  )

  trait Renderer {
    def canDisplay(node: Node, connectionSet: Set[Connection[_]]): Option[RendersWithPriority]

    def display(node: Node, connectionSet: Set[Connection[_]]): Binding[org.scalajs.dom.raw.Node]
  }

  trait DataNodeRenderer[T] extends Renderer {
    override def display(node: Node, connectionSet: Set[Connection[_]]): Binding[org.scalajs.dom.raw.Node] = {
      node match {
        case n: Data[T] ⇒ displayData(n, connectionSet)
        case _ ⇒ null
      }
    }

    def displayData(node: Data[T], connectionSet: Set[Connection[_]]): Binding[org.scalajs.dom.raw.Node]
  }

  trait RendersWithPriority {
    val over: Set[Renderer]
  }

  object RenderWithNoPriority extends RendersWithPriority {
    val over = Set()
  }
}
