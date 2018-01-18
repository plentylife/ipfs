package life.plenty.model.octopi

import life.plenty.model.connection.{Child, Parent}

class Vote(val sizeAndDirection: Int, val answer: Answer, val by: User) extends Octopus {

  override protected def preConstructor() = {
    super.preConstructor()
    println("new vote for user", by, by.id, by.connections)
    addConnection(Parent(answer))
    addConnection(Parent(by))
    answer.addConnection(Child(this))
    by.addConnection(Child(this))
  }

}

class VoteAllowance(val size: Int, val owner: User) extends Octopus {

  override protected def preConstructor() = {
    super.preConstructor()
    addConnection(Parent(owner))
    owner.addConnection(Child(this))
  }
}