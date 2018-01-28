package life.plenty.model.actions

import life.plenty.model.connection.{Amount, Creator, Parent}
import life.plenty.model.octopi._


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
    println("adding tip")
    //    new Transaction(howMuch, by, withinOctopus, basicInfo)
    //    new VoteAllowance(howMuch, by, basicInfo)
    ???
  }
}