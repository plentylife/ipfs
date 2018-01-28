package life.plenty.model.octopi

import life.plenty.model.connection.{Amount, Child, Parent}
import life.plenty.model.utils._

class Vote() extends Octopus {

  lazy val sizeAndDirection = rx.get({ case Amount(a) ⇒ a })
  lazy val parentAnswer = rx.get({ case Parent(a: Answer) ⇒ a })

  override def required = super.required ++ Set(sizeAndDirection, parentAnswer)

  onNew {
    parentAnswer.addConnection(Child(this))
    println(s"New vote added as a child to ${parentAnswer.now} | ${parentAnswer.now.get.connections}")
  }
}

//val size: Int, val owner: User, override val _basicInfo: BasicInfo
class VoteAllowance() extends Octopus {

  override protected def preConstructor() = {
    //    super.preConstructor()
    //    addConnection(Parent(owner))
    //    addConnection(Amount(size))
    //    owner.addConnection(Child(this))
  }
}