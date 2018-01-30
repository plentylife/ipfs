package life.plenty.model.octopi

import life.plenty.model.connection.{Body, Child, Connection, Title}
import rx.{Rx, Var}

trait Answer extends Space with WithParent[Space] {
  addToRequired(getBody)

  def getBody = rx.get({ case Body(b) ⇒ b })

  lazy val votes: Rx[Int] = Rx {
    val vs = rx.getAll({ case Child(v: Vote) ⇒ v })
    val mags = vs().flatMap(_.sizeAndDirection())
    mags.sum
  }

  //  rx.getWatch({ case Child(v: Vote) ⇒
  //    v
  //  }).foreach(_.foreach { v ⇒
  //    v.sizeAndDirection.debounce(100 millis).foreach({ sdOpt ⇒
  //      sdOpt foreach { sd ⇒
  //        console.trace(s"vote size added ${sd}")
  //        votes() = votes.now + sd
  //      }
  //    })
  //  })

  /** at least for now, answers do not have titles */
  override def asNew(properties: Connection[_]*): Unit = {
    setInit(Title("").inst)
    super.asNew(properties: _*)
  }
}

class Proposal extends Answer {
}

class Contribution extends Answer {
  val tips: Var[Int] = Var(0)

  rx.getWatchOnce({ case Child(t: Transaction) ⇒ t }).foreach(_.foreach(t ⇒
    t.getAmount.foreach(_.foreach(a ⇒ tips() = tips.now + a))
  ))
}