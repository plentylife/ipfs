package life.plenty.model.octopi

import life.plenty.model.connection.{Child, Parent}
import rx.Rx

class Wallet() extends Octopus {
  addToRequired(getUser)
  addToRequired(getInMembers)

  //  rx.getWatch[Parent[Octopus]].map(_.map(p ⇒ p.parent.addConnection(Child(this))))

  def getUser: Rx[Option[User]] = rx.get({ case Parent(u: User) ⇒ u })

  def getInMembers: Rx[Option[Members]] = rx.get({ case Parent(m: Members) ⇒ m })

  def registerWithParent(p: Octopus) = p.addConnection(Child(this).inst)

  onNew {
    getUser.foreach(_ foreach registerWithParent)
    getInMembers.foreach(_ foreach registerWithParent)
  }

  def getUsableThanksAmount: Int = {
    //    val thanks = (0 :: user.connections.collect({ case Child(t: Transaction) ⇒
    //      println(s"tamount ${t.amount}");
    //      if (t.from == user) -t.amount else t.amount
    //    })).sum
    //    println(s"usable thanks $thanks", user.connections)
    //    thanks
    0
  }

  def getUsableThanksLimit: Int = 50

  def getUsableVotes: Int = {
    // fixme always start at 10

    //    val allowance = user.connections.collect({ case Child(a: VoteAllowance) ⇒ a.size }).sum
    // fixme
    //    val used = user.connections.collect({ case Child(v: Vote) ⇒ v.sizeAndDirection.abs }).sum
    val used = 0
    //    allowance - used
    10
  }

  /** per day */
  def getThanksSpoilRate: Double = 0.01

  def getThanksGivenInSpace: Int = 0
}
