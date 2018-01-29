package life.plenty.model.octopi

import life.plenty.model.connection.{Body, Child, Connection, Title}
import life.plenty.model.console
import rx.Var
import rx.async.Platform._
import rx.async._

import scala.concurrent.duration._

trait Answer extends Space with WithParent[Space] {
  addToRequired(getBody)

  def getBody = rx.get({ case Body(b) ⇒ b })

  lazy val votes: Var[Int] = Var(0)

  rx.getWatch({ case Child(v: Vote) ⇒
    v
  }).foreach(_.foreach { v ⇒
    v.sizeAndDirection.debounce(100 millis).foreach({ sdOpt ⇒
      sdOpt foreach { sd ⇒
        console.trace(s"vote size added ${sd}")
        votes() = votes.now + sd
      }
    })
  })

  /** at least for now, answers do not have titles */
  override def asNew(properties: Connection[_]*): Unit = {
    set(Title("").inst)
    super.asNew(properties: _*)
  }
}

class Proposal extends Answer {
}

class Contribution extends Answer {
  def countTips: Int = 0

  //  def countTips: Int = (0 :: this.connections.collect({ case Child(t: Transaction) ⇒ t.amount })).sum
}