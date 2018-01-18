package life.plenty.model.octopi

import life.plenty.model.connection.{Child, Parent}

class Vote(val sizeAndDirection: Int, answer: Answer, by: User) extends Octopus {

  override protected def preConstructor() = {
    super.preConstructor()
    addConnection(Parent(answer))
    addConnection(Parent(by))
    answer.addConnection(Child(this))
    by.addConnection(Child(this))
  }

}
