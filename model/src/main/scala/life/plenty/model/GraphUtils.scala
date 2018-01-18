package life.plenty.model

import life.plenty.model.connection.{Connection, Parent}
import life.plenty.model.octopi.Octopus

object GraphUtils {

  //  def calculateVotes(a: Answer) =

  def findModuleUpParentTree[T](in: Octopus, matchBy: PartialFunction[Connection[_], T]): Option[T] = {
    {
      val within = in.getTopConnectionData(matchBy)
      //      println("graph utils", within, in, in.connections)
      within orElse {
        in.getTopConnectionData({ case Parent(p: Octopus) ⇒ p }) flatMap {
          p ⇒
            if (p == in) {
              println("Error in findModule of ActionAddMember: same parent")
              None
            } else {
              findModuleUpParentTree(p, matchBy)
            }
        }
      }
    }
  }
}
