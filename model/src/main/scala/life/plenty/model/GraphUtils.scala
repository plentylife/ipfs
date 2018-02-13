package life.plenty.model

import life.plenty.model.connection.{DataHub, Parent}
import life.plenty.model.octopi.definition.Hub

object GraphUtils {

  //  def calculateVotes(a: Answer) =

  def findModuleUpParentTree[T](in: Hub, matchBy: PartialFunction[DataHub[_], T]): Option[T] = {
    {
      //      println(s"graph utils", in)
      val within = in.sc.ex(matchBy)
      //                  println("graph utils", within, in, in.connections)
      within orElse {
        in.sc.ex({ case Parent(p: Hub) ⇒ p }) flatMap {
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
