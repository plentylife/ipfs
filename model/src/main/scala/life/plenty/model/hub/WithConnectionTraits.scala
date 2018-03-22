package life.plenty.model.hub

import life.plenty.model
import life.plenty.model.connection.{Amount, Child, Parent, RootParent}
import life.plenty.model.hub.definition.{Hub, Insert}
import life.plenty.model.utils.GraphUtils
import life.plenty.model.utils.DeprecatedGraphExtractors
import rx.Rx
import life.plenty.model.hub.definition.GraphOp._
import monix.reactive.Observable

trait WithParent[T <: Hub] extends WithOptParent[T] {
  addToRequired(getParent)
}

trait WithOptParent[T <: Hub] extends Hub {
  def getParent: Rx[Option[Hub]] = rx.get({ case Parent(p: Hub) ⇒ p })

  onNew {
    getParent.foreach(_.foreach { p: Hub ⇒
      model.console.trace(s"adding child to parent from ${this} to $p")
      p.addConnection(Child(this))
      DeprecatedGraphExtractors.getRootParentOrSelf(p).foreach(rp ⇒ {
        model.console.trace(s"adding child to parent from ${this} to $p | ${rp}")
        addConnection(RootParent(rp))
      })
    })
  }
}

trait WithAmount extends Hub {
  addToRequired(getAmountRx)

  @deprecated
  def getAmountRx = rx.get({ case Amount(a) ⇒ a })

  @deprecated
  def getAmountOrZeroRx: Rx[Int] = getAmountRx.map(_.getOrElse(0))

  //Observable.fromIterable(List(Insert(0)))
  def getAmountOrZero = (   Observable.fromIterable(List(Insert(0))) ++ getFeed({case Amount(a) ⇒ a}))
    .map(op ⇒ numericOp(op)).scan(0)(_ + _)
//  def getAmountFeed = getFeed({case Amount(a) ⇒ a})
}

trait WithMembers extends Space {
  lazy val getMembers = rx.get({ case Child(m: Members) ⇒ m })

  onNew {
    val m = new Members
    m.asNew(Parent(this))
  }
}