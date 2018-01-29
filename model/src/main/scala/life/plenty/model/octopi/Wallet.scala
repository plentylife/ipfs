package life.plenty.model.octopi

import life.plenty.model
import rx.{Ctx, Rx, Var}

class Wallet(u: User) {
  // fixme check
  private implicit val ctx = Ctx.Owner.Unsafe

  lazy val getUsableThanksAmount: Rx[Int] = {
    val start = Var(0)
    u.getTransactionsFrom.foreach(list ⇒ {
      model.console.println(s"Wallet parsing FROM transactions ${list}")
      list.map(_.getAmountOrZero).foreach(_.foreach(a ⇒ {
        model.console.println(s"amount ${a}")

        start() = start.now - a
      }))
    })

    u.getTransactionsTo.foreach(list ⇒ {
      model.console.println(s"Wallet parsing TO transactions ${list}")
      list.map(_.getAmountOrZero).foreach(_.foreach(a ⇒ start() = start.now + a))
    })

    start.foreach(s ⇒ model.console.println(s"start ${s}"))
    start
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
