package life.plenty.model.octopi

import life.plenty.model.connection.{Body, Child, Connection, Title}
import life.plenty.model.utils._
import rx.Rx

trait Answer extends Space with WithParent[Space] {
  override def required = super.required + getBody

  def getBody = rx.get({ case Body(b) ⇒ b })

  lazy val votes: Rx[Int] = this.rx.cons.map {
    cons: List[Connection[_]] ⇒
      val res: List[Int] = 0 :: cons.collect({ case Child(v: Vote) ⇒
        println(s"each vote ${v} ${v.connections}")
        v.sizeAndDirection().getOrElse(0)
      })
      println(s"counting votes in $this res $res ${cons}")
      res.sum
  }

  //lazy val votes: Rx[Int] = Rx {
  //    (0 :: this.rx.cons().collect({ case Child(v: Vote) ⇒
  //      v.sizeAndDirection.now.getOrElse(0): Int
  //    })).sum
  //  }

  override def idGenerator: String = {
    super.idGenerator + (getBody: String)
  }

  /** at least for now, answers do not have titles */
  override def asNew(properties: Connection[_]*): Unit = {
    set(Title("").inst)
    super.asNew(properties: _*)
  }
}

class BasicAnswer extends Answer {
}

class Contribution
  extends Answer {
  def countTips: Int = 0

  //  def countTips: Int = (0 :: this.connections.collect({ case Child(t: Transaction) ⇒ t.amount })).sum
}