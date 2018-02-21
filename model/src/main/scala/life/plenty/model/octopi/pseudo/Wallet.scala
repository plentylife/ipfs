package life.plenty.model.octopi.pseudo

import life.plenty.model
import life.plenty.model.octopi.definition.Hub
import life.plenty.model.octopi.{Space, User}
import life.plenty.model.utils.GraphUtils
import rx.{Ctx, Rx, Var}

class Wallet(u: User, space: Space)(implicit ctx: Ctx.Owner) {
//  lazy val rootSpace: Rx.Dynamic[Hub] = GraphUtils.getRootParent(space).map(_ match {
//    case Some(s) ⇒ s
//    case _ ⇒ space
//  })

  lazy val getUsableThanksAmount: Rx[Int] = Rx {
      val fromAmounts = u.getTransactionsFrom().map(t ⇒ t.getAmountOrZero())
      val toAmounts = u.getTransactionsTo().map(t ⇒ t.getAmountOrZero())

    model.console.trace(s"Wallet thanks count | to ${toAmounts} | from ${fromAmounts}")
      (0 :: fromAmounts.map(_ * -1) ::: toAmounts).sum
  }


  lazy val getUsableThanksLimit: Rx[Int] = Var(50)

  //  lazy val getUsableVotes: Rx[Int] = Rx {0}
  lazy val getUsableVotes: Rx[Int] = Rx {
    val allowances = u.getVoteAllowances().map(_.getAmountOrZero())
    val used = u.getVotes().map(v ⇒ -Math.abs(v.getAmountOrZero()))
    model.console.trace(s"Wallet usable votes | allowed ${allowances} | used ${used}")

    (10 :: allowances ::: used).sum
  }

  /** per day */
  lazy val getThanksSpoilRate: Rx[Double] = Var(0.01)

  lazy val getThanksGivenInSpace: Rx[Int] = Var(0)
}
