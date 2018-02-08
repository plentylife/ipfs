package life.plenty.model.octopi

import life.plenty.model
import life.plenty.model.connection._
import life.plenty.model.utils._

class Transaction() extends WithAmount {
  addToRequired(getOnContribution)

  def getTo = rx.get({ case To(u) ⇒ u })

  def getOnContribution = rx.get({ case Parent(c: Contribution) ⇒ c })

  def getFrom = getCreator

  override def asNew(properties: Connection[_]*): Unit = {
    properties.collectFirst {
      case Parent(c: Contribution) ⇒ {
        model.console.trace(s"New transaction setting To with ${c.getCreator.now} ${c.sc.all}")
        setInit(To(c.getCreator.now.get))
      }
    }
    super.asNew(properties: _*)
  }

  onNew {
    getFrom.addConnection(Child(this))
    getTo.addConnection(Child(this))
    getOnContribution.addConnection(Child(this))
  }

}
