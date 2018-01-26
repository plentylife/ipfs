package life.plenty.model.actions

import life.plenty.model.octopi._


class ActionUpDownVote(override val withinOctopus: Answer) extends Module[Answer] {
  def up(by: User) = {
    //    new Vote(1, withinOctopus, by, basicInfo)
    ???
  }

  def down(by: User) = {
    //    new Vote(-1, withinOctopus, by, basicInfo)
    ???
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