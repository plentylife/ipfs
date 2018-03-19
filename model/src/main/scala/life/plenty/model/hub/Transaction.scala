package life.plenty.model.hub

import life.plenty.model
import life.plenty.model.connection._
import life.plenty.model.utils._

import scala.concurrent.Future

class Transaction() extends WithAmount {
  addToRequired(getOnContribution)

  def getTo = rx.get({ case To(u) ⇒ u })
  lazy val to = getInsertStream.collect({ case To(u) ⇒ u })

  def getOnContribution = rx.get({ case Parent(c: Contribution) ⇒ c })

  def getFrom = getCreator
  lazy val from = getInsertStream.collect({ case From(u) ⇒ u })

  override def asNew(properties: DataHub[_]*): Future[Unit] = {
    var ps = properties.toList
    // adding the To connection
    properties.collectFirst {
      case Parent(c: Contribution) ⇒

        // todo. check if this ever fails
        val rp = DeprecatedGraphExtractors.getRootParentOrSelf(c).now
        val cr = c.getCreator.now
        model.console.trace(s"New transaction setting To with ${cr} ${cr.get.id} | root parent $rp | ${c.sc.lazyAll}")

        List(To(c.getCreator.now.get), RootParent(rp))
    } foreach {ps ++= _}

    super.asNew(ps: _*)
  }

  onNew {
    // very important that we don't add any more connections until the From user has been verified
    // as in they pass the FundsCheck
    getFrom.addConnection(Child(this), execOnSuccess = () ⇒ {
      model.console.trace(s"Transaction added with id ${this.id} ")
      getTo.addConnection(Child(this))
      getOnContribution.addConnection(Child(this))
    })
  }

}
