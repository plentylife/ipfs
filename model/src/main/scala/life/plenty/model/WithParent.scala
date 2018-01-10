package life.plenty.model

import life.plenty.model.actions.ActionOnInitialize
import life.plenty.model.connection.Parent

trait WithParent[T <: Octopus] extends Octopus {
  val parent: T
}

class ActionAddParent[T <: Octopus](override val withinOctopus: WithParent[T]) extends
  ActionOnInitialize[WithParent[T]] {
  override def onInitialize(): Unit = {
    //    println("adding parent")
    withinOctopus addConnection Parent(withinOctopus.parent)
  }
}
