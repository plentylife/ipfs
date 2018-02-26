package life.plenty.model.octopi

import life.plenty.model
import life.plenty.model.connection.{Body, Child, DataHub, Title}
import rx.Rx

import scala.collection.immutable
import scala.concurrent.Future

trait Answer extends Space with WithParent[Space] {
  addToRequired(getBody)

  def getBody = rx.get({ case Body(b) ⇒ b })

  lazy val allVotes = rx.getAll({ case Child(v: Vote) ⇒ v })

  lazy val votes: Rx[Int] = Rx {
    val mags = allVotes().flatMap(_.sizeAndDirection())
    model.console.trace(s"Answer votes magnitudes ${this} ${mags}")
    mags.sum
  }

  lazy val votesByUser: Rx[Map[Option[User], Int]] = Rx {
    val tuples: immutable.Seq[(Option[User], Int)] = allVotes().map(
      v ⇒ v.getCreator() → v.sizeAndDirection().getOrElse(0))
    tuples.groupBy(_._1).mapValues(_.map(_._2).sum)
  }

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
    (0 :: transactions().map({ t ⇒ t.getAmountOrZero() })).sum
  }

  // fixme make tips just count
  //  rx.getWatchOnce({ case Child(t: Transaction) ⇒ t }).foreach(_.foreach(t ⇒
  //    t.getAmount.foreach(_.foreach(a ⇒ tips() = tips.now + a))
  //  ))
}