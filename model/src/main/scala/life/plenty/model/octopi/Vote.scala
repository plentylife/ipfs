package life.plenty.model.octopi

import life.plenty.model.connection.{Child, Parent}
import life.plenty.model.utils._

class Vote() extends WithAmount {

  lazy val sizeAndDirection = getAmount
  lazy val parentAnswer = rx.get({ case Parent(a: Answer) ⇒ a })

  addToRequired(sizeAndDirection)
  addToRequired(parentAnswer)

  onNew {
    parentAnswer.addConnection(Child(this))
    getCreator.foreach(_.foreach(u ⇒ u.addConnection(Child(this))))
    println(s"New vote added as a child to ${parentAnswer.now} | ${parentAnswer.now.get.connections}")
  }
}

class VoteAllowance() extends WithAmount {

  lazy val onTransaction = rx.get({ case Parent(t: Transaction) ⇒ t })

  addToRequired(onTransaction)

  onNew {
    onTransaction.foreach(_.foreach(_.getFrom.addConnection(Child(this))))
    onTransaction.addConnection(Child(this))
  }
}