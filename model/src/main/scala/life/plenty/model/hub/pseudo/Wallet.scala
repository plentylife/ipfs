package life.plenty.model.hub.pseudo

import life.plenty.model
import life.plenty.model.hub.definition.Hub
import life.plenty.model.hub.{Space, User}
import life.plenty.model.utils.GraphUtils
import rx.{Ctx, Rx, Var}

class Wallet(u: User, space: Hub)(implicit ctx: Ctx.Owner) {
//  lazy val rootSpace: Rx.Dynamic[Hub] = GraphUtils.getRootParent(space).map(_ match {
//    case Some(s) ⇒ s
//    case _ ⇒ space
//  })

  // fixme. right now this relies on the ui filter, which is not right security wise

  lazy val getThanksBalance: Rx[Int] = Rx {
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

    (5 :: allowances ::: used).sum
  }

  /** per day */
  lazy val getThanksSpoilRate: Rx[Double] = Var(0.01)
}
