package life.plenty.model.hub

import life.plenty.model
import life.plenty.model.connection.{Body, Child, DataHub, Title}
import rx.Rx

import scala.collection.immutable
import scala.concurrent.Future
import life.plenty.model.hub.definition.GraphOp._
trait Answer extends Space with WithParent[Space] {
  addToRequired(getBody)

  def getBody = rx.get({ case Body(b) ⇒ b })
  lazy val body = getInsertFeed.collect {case Body(t) ⇒ t}

  lazy val allVotes = rx.getAll({ case Child(v: Vote) ⇒ v })

  @deprecated
  lazy val votesRx: Rx[Int] = Rx {
    val mags = allVotes().flatMap(_.sizeAndDirection())
    model.console.trace(s"Answer votes magnitudes ${this} ${mags}")
    mags.sum
  }

  // todo. account removes
  lazy val voteSum = getFeed({case Child(v: Vote) ⇒ v}).depMapLast(_.getAmountOrZero).scanToList
    .map(list ⇒ (0 :: list).sum)

  /** at least for now, answers do not have titles */
  override def asNew(properties: DataHub[_]*): Future[Unit] = {
    val ps = Title("") :: properties.toList
    super.asNew(ps: _*)
  }
}

class Proposal extends Answer {
}

class Contribution extends Answer {
  private lazy val transactions = rx.getAll({ case Child(t: Transaction) ⇒ t })
  // check this might fail
  lazy val tips: Rx[Int] = Rx {
    (0 :: transactions().map({ t ⇒ t.getAmountOrZeroRx() })).sum
  }

  // fixme make tips just count
  //  rx.getWatchOnce({ case Child(t: Transaction) ⇒ t }).foreach(_.foreach(t ⇒
  //    t.getAmount.foreach(_.foreach(a ⇒ tips() = tips.now + a))
  //  ))
}