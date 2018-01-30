package life.plenty.model.octopi

import life.plenty.model
import rx.{Ctx, Rx, Var}

class Wallet(u: User)(implicit ctx: Ctx.Owner) {

  lazy val getUsableThanksAmount: Rx[Int] = Rx {
    //    Rx {
      val fromAmounts = u.getTransactionsFrom().map(t ⇒ t.getAmountOrZero())
      val toAmounts = u.getTransactionsTo().map(t ⇒ t.getAmountOrZero())

    model.console.trace(s"Wallet thanks count | to ${toAmounts} | from ${fromAmounts}")
      (0 :: fromAmounts.map(_ * -1) ::: toAmounts).sum
    //    }
    //    0
  }

  lazy val getUsableThanksLimit: Rx[Int] = Var(50)

  //  lazy val getUsableVotes: Rx[Int] = Rx {0}
  lazy val getUsableVotes: Rx[Int] = Rx {
    val allowances = u.getVoteAllowances().map(_.getAmountOrZero())
    val used = u.getVotes().map(_.getAmountOrZero())
    model.console.println(s"Wallet usable votes | allowed ${allowances} | used ${used}")

    (10 :: allowances ::: used.map(_ * -1)).sum
  }

  /** per day */
  lazy val getThanksSpoilRate: Rx[Double] = Var(0.01)

  lazy val getThanksGivenInSpace: Rx[Int] = Var(0)
}
