package life.plenty.model

trait WithParent[T <: Octopus] extends Octopus {
  val parent: T
  // this has become a module
  //  addConnection(Parent(parent))
}

class ActionAddParent[T <: Octopus](override val withinOctopus: WithParent[T]) extends
  ActionOnInitialize[WithParent[T]] {
  override def onInitialize(): Unit = {
    //    println("adding parent")
    withinOctopus addConnection Parent(withinOctopus.parent)
  }
}
