package life.plenty.model.actions

import life.plenty.model._
import life.plenty.model.connection.{Child, Connection, Contributor, Parent}

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
    //    println("trying to find member module in", starting)
    val within = starting.getTopConnectionData({ case Child(m: Members) ⇒ m })
    //    if (within.nonEmpty) println("found members octopus") else println(starting.connections)
    within orElse {
      starting.getTopConnectionData({ case Parent(p: Octopus) ⇒ p }) flatMap {
        p ⇒
          if (p == starting) {
            println("Error in findModule of ActionAddMember: same parent")
            None
          } else {
            findModule(p)
          }
      }
    }
  }
}

class ActionAddParent[T <: Octopus](override val withinOctopus: WithParent[T]) extends
  ActionOnInitialize[WithParent[T]] {
  override def onInitialize(): Unit = {
    //    println("adding parent")
    withinOctopus addConnection Parent(withinOctopus.parent)
  }
}
