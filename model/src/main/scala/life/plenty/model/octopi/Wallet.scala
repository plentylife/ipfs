package life.plenty.model.octopi

import life.plenty.model.connection.{Child, Parent}

class Wallet(user: User, inMembers: Members) extends Octopus {
  def getUsableThanksAmount: Int = 0

  def getUsableThanksLimit: Int = 50

  def getUsableVotes: Int = {
    val allowance = user.connections.collect({ case Child(a: VoteAllowance) ⇒ a.size }).sum
    val used = user.connections.collect({ case Child(v: Vote) ⇒ v.sizeAndDirection.abs }).sum
    println("wallet all used", allowance, used)
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
