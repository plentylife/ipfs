package life.plenty.model.octopi

import life.plenty.model.connection._
import life.plenty.model.utils._

// val amount: Int, val from: User, val on: Contribution, override val _basicInfo: BasicInfo
class Transaction() extends WithAmount {
  addToRequired(getOnContribution)

  def getTo = rx.get({ case To(u) ⇒ u })

  def getOnContribution = rx.get({ case Parent(c: Contribution) ⇒ c })

  def getFrom = getCreator

  override def asNew(properties: Connection[_]*): Unit = {
    //    properties.collectFirst{
    //      case Parent(c: Contribution) ⇒ set(To(c.getCreator.now.get))
    //    }
    super.asNew(properties: _*)
  }

  onNew {
    //    getFrom.addConnection(Child(this))
    getTo.addConnection(Child(this))
    getOnContribution.addConnection(Child(this))
  }

  override protected def preConstructor(): Unit = {
    //    super.preConstructor()
    //    addConnection(Amount(amount))
    //    addConnection(From(from))
    //    addConnection(To(on.creator()))
    //    println("adding transaction to")
    //    addConnection(Parent(on))
    //    println("--- adding transaction")
    //    from.addConnection(Child(this))
    //    on.creator().addConnection(Child(this))
    //    on.addConnection(Child(this))
  }
}
