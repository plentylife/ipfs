package life.plenty.model.octopi

import life.plenty.model.connection.{Body, Child}
import life.plenty.model.utils.Property

trait Answer extends Space with WithParent[Space] {
  protected val _body: String
  lazy val body = new Property[String]({ case Body(t: String) ⇒ t }, this, _body)

  def countVotes: Int = (0 :: this.connections.collect({ case Child(v: Vote) ⇒ v.sizeAndDirection })).sum

  override def idGenerator: String = {
    //    println("answer id gen", _body, body.init, body.getSafe)
    super.idGenerator + body()
  }

  override protected def preConstructor() = {
    super.preConstructor()
    body applyInner { b ⇒ addConnection(Body(b)) }
  }
}

class BasicAnswer(override val _parent: Space, override val _body: String) extends Answer {
  /** at least for now, answers do not have titles */
  override lazy val _title: String = ""
}

class Contribution(override val _parent: Space, override val _body: String, override val creator: User) extends Answer
  with WithCreator {
  def countTips: Int = (0 :: this.connections.collect({ case Child(t: Transaction) ⇒ t.amount })).sum

  override lazy val _title: String = ""
}