package life.plenty.model.octopi

import life.plenty.model.connection.Member
import rx.{Ctx, Rx}

class Members extends WithParent[Space] {
  lazy val getMembers: Rx[List[User]] = rx.getAll({ case Member(u) ⇒ u })

  Members.register(this)
}

object Members {
  implicit val ctx: Ctx.Owner = Ctx.Owner.safe()

  private var constituents = Set[Members]()

  def register(m: Members) = {
    println("registering members")
    constituents += m
    taskList.foreach(t ⇒ t(m, ctx))
  }

  private var taskList = List[(Members, Ctx.Owner) ⇒ Unit]()

  def proposeTask(task: (Members, Ctx.Owner) ⇒ Unit): Unit = {
    taskList = task :: taskList
    constituents foreach { m ⇒
      task(m, ctx)
    }
  }
}

