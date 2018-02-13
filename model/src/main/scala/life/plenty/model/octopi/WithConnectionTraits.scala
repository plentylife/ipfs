package life.plenty.model.octopi

import life.plenty.model
import life.plenty.model.connection.{Amount, Child, Parent}
import life.plenty.model.octopi.definition.Hub
import rx.Rx

trait WithParent[T <: Hub] extends Hub {
  addToRequired(getParent)

  def getParent: Rx[Option[Hub]] = rx.get({ case Parent(p: Hub) ⇒ p })

  //  override def idGenerator: String = super.idGenerator + getParent.now.get.id

  onNew {
    getParent.foreach(_.foreach { p: Hub ⇒
      model.console.trace(s"adding child to parent from ${this} to $p")
      p.addConnection(Child(this))
    })
  }
}

trait WithAmount extends Hub {
  addToRequired(getAmount)

  def getAmount = rx.get({ case Amount(a) ⇒ a })

  def getAmountOrZero: Rx[Int] = getAmount.map(_.getOrElse(0))
}

trait WithMembers extends Space {
  lazy val getMembers = rx.get({ case Child(m: Members) ⇒ m })

  onNew {
    val m = new Members
    m.asNew(Parent(this))
  }
}