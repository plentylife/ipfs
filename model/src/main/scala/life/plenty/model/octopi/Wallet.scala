package life.plenty.model.octopi

import rx.{Rx, Var}

class Wallet(u: User) {
  lazy val getUsableThanksAmount: Rx[Int] = {
    //    val thanks = (0 :: user.connections.collect({ case Child(t: Transaction) ⇒
    //      println(s"tamount ${t.amount}");
    //      if (t.from == user) -t.amount else t.amount
    //    })).sum
    //    println(s"usable thanks $thanks", user.connections)
    //    thanks
    Var(0)
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
