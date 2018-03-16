package life.plenty.model.actions

import life.plenty.model.connection.{Amount, Creator, Parent}
import life.plenty.model.hub._
import life.plenty.model.hub.definition.Module


class ActionUpDownVote(override val hub: Answer) extends Module[Answer] {
  def up() = {
    val v = new Vote
    v.asNew(Amount(1), Parent(hub))
  }

  def down() = {
    val v = new Vote
    v.asNew(Amount(-1), Parent(hub))
  }
}

class ActionGiveThanks(override val hub: Contribution) extends Module[Contribution] {
  def add(howMuch: Int, by: User) = {
    if (howMuch < 1) throw new Exception("Amount has to be more than 0")
    val t = new Transaction()
    println(s"NEW TRANSACTION WITH parent ${hub} ${hub.id}")
    t.asNew(Parent(hub), Amount(howMuch))

    t.onNew {
      println(s"NEW TRANSACTION ${t.sc.lazyAll}")
      val va = new VoteAllowance()
      va.asNew(Parent(t), Amount(howMuch))
    }
  }
}