package life.plenty.model.octopi

import life.plenty.model.connection.{Child, Parent}

class Wallet(user: User, inMembers: Members, override val _basicInfo: BasicInfo) extends Octopus {
  def getUsableThanksAmount: Int = {
    val thanks = (0 :: user.connections.collect({ case Child(t: Transaction) ⇒
      println(s"tamount ${t.amount}");
      if (t.from == user) -t.amount else t.amount
    })).sum
    println(s"usable thanks $thanks", user.connections)
    thanks
  }

  def getUsableThanksLimit: Int = 50

  def getUsableVotes: Int = {
    val allowance = user.connections.collect({ case Child(a: VoteAllowance) ⇒ a.size }).sum
    // fixme
    //    val used = user.connections.collect({ case Child(v: Vote) ⇒ v.sizeAndDirection.abs }).sum
    val used = 0
    allowance - used
  }

  /** per day */
  def getThanksSpoilRate: Double = 0.01

  def getThanksGivenInSpace: Int = 0

  override protected def preConstructor(): Unit = {
    super.preConstructor()
    addConnection(Parent(user))
    addConnection(Parent(inMembers))
  }
}
