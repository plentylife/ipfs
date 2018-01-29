package life.plenty.model.octopi

import life.plenty.model
import rx.{Ctx, Rx, Var}

class Wallet(u: User)(implicit ctx: Ctx.Owner) {

  lazy val getUsableThanksAmount: Rx[Int] = {
    Rx {
      val fromAmounts = u.getTransactionsFrom().map(t ⇒ t.getAmountOrZero())
      val toAmounts = u.getTransactionsTo().map(t ⇒ t.getAmountOrZero())

      model.console.println(s"Wallet thanks count | to ${toAmounts} | from ${fromAmounts}")
      (0 :: fromAmounts.map(_ * -1) ::: toAmounts).sum
    }
  }

  lazy val getUsableThanksLimit: Rx[Int] = Var(50)

  lazy val getUsableVotes: Rx[Int] = {
    // fixme always start at 10

    //    val allowance = user.connections.collect({ case Child(a: VoteAllowance) ⇒ a.size }).sum
    // fixme
    //    val used = user.connections.collect({ case Child(v: Vote) ⇒ v.sizeAndDirection.abs }).sum
    val used = 0
    //    allowance - used
    Var(10)
  }

  /** per day */
  lazy val getThanksSpoilRate: Rx[Double] = Var(0.01)

  lazy val getThanksGivenInSpace: Rx[Int] = Var(0)
}
