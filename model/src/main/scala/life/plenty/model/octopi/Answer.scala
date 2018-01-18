package life.plenty.model.octopi

import life.plenty.model.connection.{Body, Child}

trait Answer extends Space with WithParent[Space] {
  val body: String

  def countVotes = (0 :: this.connections.collect({ case Child(v: Vote) ⇒ v.sizeAndDirection })).sum

  override protected def preConstructor() = {
    super.preConstructor()
    addConnection(Body(body))
  }
}

class BasicAnswer(override val parent: Space, override val body: String) extends Answer {
  /** at least for now, answers do not have titles */
  override val title: String = ""
}

class Contribution(override val parent: Space, override val body: String, override val creator: User) extends Answer
  with WithCreator {
  def countTips: Int = (0 :: this.connections.collect({ case Child(t: Transaction) ⇒ t.amount })).sum

  override val title: String = ""
}