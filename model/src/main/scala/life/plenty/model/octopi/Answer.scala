package life.plenty.model.octopi

import life.plenty.model.connection.{Body, Child, Connection, Title}
import life.plenty.model.utils._
import rx.{Rx, Var}

trait Answer extends Space with WithParent[Space] {
  override def required = super.required + getBody

  def getBody = rx.get({ case Body(b) ⇒ b })

  lazy val votes: Rx[Int] = Rx {
    (0 :: this._connections().collect({ case Child(v: Vote) ⇒
      //      val rx: Rx[Int] = v.sizeAndDirection.getOrElse(0)
      val rx: Rx[Int] = Var(0)
      rx(): Int
    })).sum
  }

  override def idGenerator: String = {
    super.idGenerator + (getBody: String)
  }

  /** at least for now, answers do not have titles */
  override def asNew(properties: Connection[_]*): Unit = {
    set(Title(""))
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