package life.plenty.model.octopi

import life.plenty.model.connection.Parent

class Wallet(user: User, inMembers: Members) extends Octopus {
  def getUsableThanksAmount: Int = 0

  def getUsableThanksLimit: Int = 50

  def getUsableVotes: Int = 10

  /** per day */
  def getThanksSpoilRate = 0.01

  def getThanksGivenInSpace: Int = 0

  override protected def preConstructor(): Unit = {
    super.preConstructor()
    addConnection(Parent(user))
    addConnection(Parent(inMembers))
  }
}
