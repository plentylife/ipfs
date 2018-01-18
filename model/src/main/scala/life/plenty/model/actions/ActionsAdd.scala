package life.plenty.model.actions

import life.plenty.model.GraphUtils
import life.plenty.model.connection.{Child, Connection, Contributor, Parent}
import life.plenty.model.octopi._

class ActionAddContributor(override val withinOctopus: Contribution) extends Module[Contribution] {
  def add(userId: String) = {
    val u = new BasicUser(userId)
    val existing = withinOctopus.connections.collect({ case Contributor(u) ⇒ u })
    if (!existing.contains(u)) {
      withinOctopus.addConnection(Contributor(u))
      println("contributor added ", withinOctopus.connections)
    }
  }
}

class ActionAddMember(override val withinOctopus: Octopus) extends ActionAfterGraphTransform {
  private lazy val membersOctopus = {
    findModule()
  }
  override def onConnectionAdd(connection: Connection[_]): Either[Exception, Unit] =
    connection match {
      case Contributor(c: User) ⇒ addMember(c)
      //      case Child(o: Octopus) ⇒
      case _ ⇒ Right()
    }
  private def addMember(u: User) = membersOctopus match {
    case Some(o) ⇒ o.addMember(u); Right()
    case _ ⇒ Left(new Exception("no Members octopus"))
  }
  override def onConnectionRemove(connection: Connection[_]): Either[Exception, Unit] = ???

  private def findModule(starting: Octopus = withinOctopus): Option[Members] = {
    GraphUtils.findModuleUpParentTree(starting, { case Child(m: Members) ⇒ m })
  }
}

class ActionAddParent[T <: Octopus](override val withinOctopus: WithParent[T]) extends
  ActionOnInitialize[WithParent[T]] {
  override def onInitialize(): Unit = {
    //    println("adding parent")
    withinOctopus addConnection Parent(withinOctopus.parent)
  }
}
