package life.plenty.model.octopi

import life.plenty.model
import life.plenty.model.connection._
import life.plenty.model.utils._

class Transaction() extends WithAmount {
  addToRequired(getOnContribution)

  def getTo = rx.get({ case To(u) ⇒ u })

  def getOnContribution = rx.get({ case Parent(c: Contribution) ⇒ c })

  def getFrom = getCreator

  override def asNew(properties: DataHub[_]*): Unit = {
    var ps = properties.toList
    properties.collectFirst {
      case Parent(c: Contribution) ⇒ {
        model.console.trace(s"New transaction setting To with ${c.getCreator.now} ${c.sc.all}")
        To(c.getCreator.now.get)
      }
    } foreach {ps +:= _}

    super.asNew(ps: _*)
  }

  onNew {
    getFrom.addConnection(Child(this))
    getTo.addConnection(Child(this))
    getOnContribution.addConnection(Child(this))
  }

}
