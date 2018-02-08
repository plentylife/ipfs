package life.plenty.model.actions

import life.plenty.model.connection.{Amount, Creator, Parent}
import life.plenty.model.octopi._
import life.plenty.model.octopi.definition.Module


class ActionUpDownVote(override val withinOctopus: Answer) extends Module[Answer] {
  def up(by: User) = {
    val v = new Vote
    v.asNew(Amount(1), Parent(withinOctopus), Creator(by))
  }

  def down(by: User) = {
    val v = new Vote
    v.asNew(Amount(-1), Parent(withinOctopus), Creator(by))
  }
}

class ActionTip(override val withinOctopus: Contribution) extends Module[Contribution] {
  def add(howMuch: Int, by: User) = {
    if (howMuch < 1) throw new Exception("Amount has to be more than 0")
    val t = new Transaction()
    t.asNew(Parent(withinOctopus), Amount(howMuch))
    val va = new VoteAllowance()
    va.asNew(Parent(t), Amount(howMuch))
  }
}